package de.schub.docker_controller.Metadata.Collector;

import de.schub.docker_controller.Metadata.Exception.MetadataCollectorException;
import de.schub.docker_controller.Metadata.Exception.UnkownEndpointException;

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
    public MetadataCollector getCollector(URI endpoint)
    {
        for (MetadataCollectorProvider provider : providers) {
            if (provider.supports(endpoint)) {
                return provider.getCollector(endpoint);
            }
        }

        return null;
    }
}
