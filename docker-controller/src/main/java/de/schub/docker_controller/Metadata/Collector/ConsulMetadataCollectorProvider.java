package de.schub.docker_controller.Metadata.Collector;

import com.orbitz.consul.Consul;
import de.schub.docker_controller.Metadata.ConsulClientFactory;
import de.schub.docker_controller.Metadata.ContainerMetadata;
import de.schub.docker_controller.Metadata.Exception.MetadataCollectorException;

import javax.inject.Inject;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class ConsulMetadataCollectorProvider implements MetadataCollectorProvider
{
    private final ConsulClientFactory consulClientFactory;

    @Inject
    public ConsulMetadataCollectorProvider(ConsulClientFactory consulClientFactory)
    {
        this.consulClientFactory = consulClientFactory;
    }

    @Override
    public MetadataCollector getCollector(URI endpoint)
    {
        String prefix = endpoint.getPath();
        if (null == prefix || 0 == prefix.length()) {
            prefix = "container";
        }

        return new ConsulMetadataCollector(consulClientFactory.get(endpoint), prefix);
    }

    @Override
    public boolean supports(URI endpoint)
    {
        return endpoint.getScheme().equals("consul");
    }

    class ConsulMetadataCollector implements MetadataCollector
    {
        private final Consul consul;
        private final String prefix;

        public ConsulMetadataCollector(Consul consul, String prefix)
        {
            this.consul = consul;
            this.prefix = prefix;
        }

        @Override
        public ContainerMetadata get(String containerId) throws MetadataCollectorException
        {
            return null;
        }

        @Override
        public List<ContainerMetadata> getAll() throws MetadataCollectorException
        {
            List<String> metadata = consul.keyValueClient().getValuesAsString(prefix);

            return null;
        }

        @Override
        public Map<String, ContainerMetadata> getMap() throws MetadataCollectorException
        {
            return null;
        }
    }
}
