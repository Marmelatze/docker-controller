package de.schub.docker_controller.Metadata.Collector;

import com.orbitz.consul.Consul;
import de.schub.docker_controller.Metadata.ConsulClientFactory;
import de.schub.docker_controller.Metadata.ContainerMetadata;
import de.schub.docker_controller.Metadata.Exception.MetadataCollectorException;

import javax.inject.Inject;
import java.net.URI;
import java.util.List;

/**
 * Get all docker hosts from consul server.
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
        return new ConsulDockerMetadataCollector(consulClientFactory.get(endpoint));
    }

    @Override
    public boolean supports(URI endpoint)
    {
        return endpoint.getScheme().equals("consul+docker");
    }

    class ConsulDockerMetadataCollector implements MetadataCollector
    {
        private final Consul consulClient;

        public ConsulDockerMetadataCollector(Consul consulClient)
        {
            this.consulClient = consulClient;
        }

        @Override
        public ContainerMetadata get(String containerId) throws MetadataCollectorException
        {
            return null;
        }

        @Override
        public List<ContainerMetadata> getAll() throws MetadataCollectorException
        {
            return null;
        }
    }
}
