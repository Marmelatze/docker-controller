package de.schub.docker_controller.Metadata.Collector;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.Info;
import de.schub.docker_controller.Metadata.ContainerMetadata;
import de.schub.docker_controller.Metadata.DockerClientFactory;
import de.schub.docker_controller.Metadata.Exception.MetadataCollectorException;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

public class DockerMetadataCollectorProvider implements MetadataCollectorProvider
{
    protected final DockerClientFactory dockerClientFactory;
    protected final String hostname;
    Logger logger = LoggerFactory.getLogger(DockerMetadataCollectorProvider.class);

    @Inject
    public DockerMetadataCollectorProvider(DockerClientFactory dockerClientFactory, @Named("hostname") String hostname)
    {
        this.dockerClientFactory = dockerClientFactory;
        this.hostname = hostname;
    }

    @Override
    public MetadataCollector getCollector(URI endpoint)
    {
        return new DockerMetadataCollector(dockerClientFactory.get(parseURI(endpoint)));
    }

    protected URI parseURI(URI endpoint)
    {
        URIBuilder builder = new URIBuilder(endpoint);
        try {
            if (null == endpoint.getHost()) {
                return builder.setScheme("unix").build();
            }
            builder.setScheme("http");
            return builder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("unable to build endpoint url", e);
        }
    }

    @Override
    public boolean supports(URI endpoint)
    {
        return endpoint.getScheme().equals("docker");
    }

    class DockerMetadataCollector implements MetadataCollector
    {
        DockerClient dockerClient;

        boolean connected = false;

        public DockerMetadataCollector(DockerClient dockerClient)
        {
            this.dockerClient = dockerClient;
        }

        @Override
        public ContainerMetadata get(String containerId) throws MetadataCollectorException
        {
            connect();
            ContainerInfo container;
            try {
                container = dockerClient.inspectContainer(containerId);
            } catch (DockerException | InterruptedException e) {
                throw new MetadataCollectorException("Unable to inpect container " + containerId, e);
            }

            ContainerMetadata.ContainerMetadataBuilder builder = ContainerMetadata.builder()
                .setContainerId(container.id())
                .setHost(hostname)
                .setIp(container.networkSettings().ipAddress())
                // remove / at the beginning
                .setName(container.name().substring(1))
                ;

            // loop through env variables
            for (String env : container.config().env()) {
                String[] parts = env.split("=");
                String key = parts[0];
                String value = parts[1];
                switch (key) {
                    case "MESOS_TASK_ID":
                        builder.setMesosTaskId(value);
                        break;
                    case "MARATHON_APP_ID":
                        builder.setMarathonAppId(value);
                        break;
                    case "MARATHON_APP_VERSION":
                        builder.setMarathonVersion(value);
                        break;
                }
            }

            return builder.build();
        }

        @Override
        public List<ContainerMetadata> getAll() throws MetadataCollectorException
        {
            connect();
            try {
                List<Container> containers = dockerClient.listContainers();
                return containers
                    .stream()
                    .map(
                        container -> {
                            try {
                                return get(container.id());
                            } catch (MetadataCollectorException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    )
                    .collect(Collectors.toList());
            } catch (DockerException | InterruptedException e) {
                throw new MetadataCollectorException("Failed to retrieve container list", e);
            }
        }

        protected void connect() throws MetadataCollectorException
        {
            if (connected) {
                return;
            }
            try {
                connected = true;
                Info dockerInfo = dockerClient.info();
                logger.info("Connected to docker server " + dockerInfo.toString());

            } catch (DockerException | InterruptedException e) {
                throw new MetadataCollectorException("Failed to connect to Docker-Daemon", e);
            }
        }
    }
}