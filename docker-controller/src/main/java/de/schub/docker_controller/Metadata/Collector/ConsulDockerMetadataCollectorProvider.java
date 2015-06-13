package de.schub.docker_controller.Metadata.Collector;

import com.orbitz.consul.Consul;
import com.orbitz.consul.model.health.ServiceHealth;
import de.schub.docker_controller.Metadata.ConsulClientFactory;
import de.schub.docker_controller.Metadata.ContainerMetadata;
import de.schub.docker_controller.Metadata.Exception.MetadataCollectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Get all docker hosts from consul server.
 * URL:
 * * docker+consul://HOST:PORT/SERVICE_NAME
 *
 */
public class ConsulDockerMetadataCollectorProvider implements MetadataCollectorProvider
{
    private final ConsulClientFactory consulClientFactory;
    private final DockerMetadataCollectorProvider dockerMetadataCollectorProvider;

    @Inject
    public ConsulDockerMetadataCollectorProvider(
        ConsulClientFactory consulClientFactory,
        DockerMetadataCollectorProvider dockerMetadataCollectorProvider)
    {
        this.consulClientFactory = consulClientFactory;
        this.dockerMetadataCollectorProvider = dockerMetadataCollectorProvider;
    }

    @Override
    public MetadataCollector getCollector(URI endpoint)
    {
        String serviceName = endpoint.getPath();
        if (null == serviceName || serviceName.isEmpty()) {
            serviceName = "docker";
        }
        return new ConsulDockerMetadataCollector(consulClientFactory.get(endpoint), serviceName);
    }

    @Override
    public boolean supports(URI endpoint)
    {
        return endpoint.getScheme().equals("consul+docker");
    }

    class ConsulDockerMetadataCollector implements MetadataCollector
    {
        Logger logger = LoggerFactory.getLogger(ConsulDockerMetadataCollector.class);

        private final Consul consulClient;
        private final String serviceName;

        private HashMap<String, MetadataCollector> dockerMetadataCollectors = new HashMap<>();
        private boolean connected = false;

        public ConsulDockerMetadataCollector(Consul consulClient, String serviceName)
        {
            this.consulClient = consulClient;
            this.serviceName = serviceName;
        }

        private void connect()
        {
            if (connected) {
                return;
            }
            connected = true;
            List<ServiceHealth> services = consulClient
                .healthClient()
                .getHealthyServiceInstances(serviceName)
                .getResponse();
            for (ServiceHealth service : services) {
                dockerMetadataCollectors.put(
                    service.getNode().getNode(),
                    dockerMetadataCollectorProvider.getCollector(
                        URI.create("docker://" + service.getNode().getAddress() + ":" + service.getService().getPort())
                    )
                );
            }
        }

        @Override
        public ContainerMetadata get(String containerId) throws MetadataCollectorException
        {
            connect();
            for (Map.Entry<String, MetadataCollector> entry : dockerMetadataCollectors.entrySet()) {
                MetadataCollector collector = entry.getValue();
                try {
                    ContainerMetadata metadata = collector.get(containerId);
                    if (null != metadata) {
                        return metadata;
                    }
                } catch (MetadataCollectorException e) {
                    logger.error("failed to get metadata from node " + entry.getKey(), e);
                }
            }
            return null;
        }

        @Override
        public List<ContainerMetadata> getAll() throws MetadataCollectorException
        {
            connect();
            ArrayList<ContainerMetadata> containers = new ArrayList<>();
            for (Map.Entry<String, MetadataCollector> entry : dockerMetadataCollectors.entrySet()) {
                MetadataCollector collector = entry.getValue();
                try {
                    containers.addAll(collector.getAll());
                } catch (MetadataCollectorException e) {
                    logger.error("failed to get metadata from node " + entry.getKey(), e);
                }
            }

            return containers;
        }

        @Override
        public Map<String, ContainerMetadata> getMap() throws MetadataCollectorException
        {
            return null;
        }
    }
}
