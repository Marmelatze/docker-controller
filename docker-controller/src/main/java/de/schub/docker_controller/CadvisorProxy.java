package de.schub.docker_controller;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.schub.docker_controller.Metadata.Collector.MetadataCollector;
import de.schub.docker_controller.Metadata.ContainerMetadata;
import de.schub.docker_controller.Metadata.Exception.MetadataCollectorException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adding more labels to metrics exported by cadvisor to prometheus. No longer needed as it was integrated to cadvisor
 * in PR #780: https://github.com/google/cadvisor/pull/780
 */
@Deprecated
public class CadvisorProxy
{
    private final int port;
    private final MetadataCollector collector;
    private final String cadvisor;
    Logger logger = LoggerFactory.getLogger(CadvisorProxy.class);

    public CadvisorProxy(MetadataCollector collector, int port, String cadvisor)
    {
        this.port = port;
        this.collector = collector;
        this.cadvisor = cadvisor;
    }

    public void run()
    {
        try {
            logger.info("creating http server on port " + port);
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/metrics", new MetricHander());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            logger.error("Failed to create http server ", e);
        }
    }

    private class MetricHander implements HttpHandler
    {
        WebTarget target;

        public MetricHander()
        {
            Client client = ClientBuilder.newBuilder().build();
            target = client.target(cadvisor + "/metrics");
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException
        {
            try {
                logger.info("new request from " + httpExchange.getRemoteAddress());
                String response = enhanceMetrics(target.request().get(String.class));

                Headers headers = httpExchange.getResponseHeaders();
                headers.add("Content-Type", "text/plain; version=0.0.4");
                httpExchange.sendResponseHeaders(200, response.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (Exception e) {
                logger.error("failed to reply", e);
                String response = ExceptionUtils.getFullStackTrace(e);
                httpExchange.sendResponseHeaders(500, response.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private String enhanceMetrics(String metrics)
        {
            Map<String, ContainerMetadata> containers = new HashMap<>();
            try {
                containers = collector.getMap();
            } catch (MetadataCollectorException e) {
                logger.error("Failed to get container metadata", e);
            }

            StringBuffer resultString = new StringBuffer();
            Pattern regex = Pattern.compile("\\{(.*?)\\}");
            Matcher regexMatcher = regex.matcher(metrics);

            HashMap<String, Integer> appCount = new HashMap<>();

            while (regexMatcher.find()) {
                String labelString = regexMatcher.group(1);
                // split key=value,key2=value2
                Map<String, String> labels = new HashMap<>(
                    Splitter.on(",")
                        .omitEmptyStrings()
                        .trimResults()
                        .withKeyValueSeparator("=")
                        .split(labelString)
                );
                if (!labels.containsKey("name")) {
                    continue;
                }
                String id = labels.get("id");
                if (!id.startsWith("\"/docker")) {
                    continue;
                }

                String image = "";
                String marathonApp = "";
                String marathonVersion = "";

                if (id.length() > 9) {
                    // "docker/asdfasdfasdf" -> asdfasdfasdf
                    String containerId = id.substring(9, id.length() - 1);
                    //System.out.println(containerId);
                    if (containers.containsKey(containerId)) {
                        ContainerMetadata metadata = containers.get(containerId);
                        image = metadata.getImage();
                        if (null != metadata.getMarathonAppId()) {
                            marathonApp = metadata.getMarathonAppId();
                        }
                        if (null != metadata.getMarathonVersion()) {
                            marathonVersion = metadata.getMarathonVersion();
                        }
                    }
                }

                // labels must be quoted
                labels.put("image", "\"" + image + "\"");
                labels.put("marathon_app", "\"" + marathonApp + "\"");
                labels.put("marathon_version", "\"" + marathonVersion + "\"");

                labelString = Joiner.on(",")
                    .withKeyValueSeparator("=")
                    .join(labels)
                ;

                regexMatcher.appendReplacement(resultString, "{" + labelString + "}");
            }
            regexMatcher.appendTail(resultString);


            // add number of running containers
            String runningContainers = "# HELP container_running Number of running containers.\n" +
                                       "# TYPE container_running gauge\n";

            for (ContainerMetadata container : containers.values()) {
                HashMap<String, String> labels = new HashMap<>();
                labels.put("id", container.getContainerId());
                labels.put("name", container.getName());
                labels.put("image", container.getImage());
                labels.put("marathon_app", container.getMarathonAppId());
                labels.put("marathon_version", container.getMarathonVersion());

                String labelString = Joiner.on("\",")
                    .withKeyValueSeparator("=\"")
                    .useForNull("")
                    .join(labels);
                runningContainers += "container_running{" + labelString + "\"} 1\n";
            }

            resultString.append(runningContainers);

            return resultString.toString();
        }
    }
}
