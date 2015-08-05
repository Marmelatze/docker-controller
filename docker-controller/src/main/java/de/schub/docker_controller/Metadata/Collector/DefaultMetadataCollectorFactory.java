package de.schub.docker_controller.Metadata.Collector;

import javax.inject.Inject;
import java.net.URI;
import java.util.List;

/**
 * supported endpoints:
 *   * docker://host:port
 *   * docker:///var/run/docker.sock
 *   * consul+docker://host:port/[service]
 */
public class DefaultMetadataCollectorFactory implements MetadataCollectorFactory
{
    List<MetadataCollectorProvider> providers;

    @Inject
    public DefaultMetadataCollectorFactory(List<MetadataCollectorProvider> providers)
    {
        this.providers = providers;
    }

    @Override
    public MetadataCollector get(URI endpoint)
    {
        for (MetadataCollectorProvider provider : providers) {
            if (provider.supports(endpoint)) {
                return provider.getCollector(endpoint);
            }
        }

        return null;
    }
}
