package de.schub.docker_controller.Metadata.Collector;

import javax.inject.Inject;
import java.net.URI;
import java.util.List;

/**
 * supported endpoints:
 *   * consul://HOST:PORT/PREFIX
 *   * docker://HOST:PORT
 *   * docker:///var/run/docker.sock
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
