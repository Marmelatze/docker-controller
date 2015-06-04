package de.schub.docker_controller.Metadata.Collector;

import de.schub.docker_controller.Metadata.Exception.MetadataCollectorException;
import de.schub.docker_controller.Metadata.Exception.UnkownEndpointException;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * supported endpoints:
 *   * consul://HOST:PORT/PREFIX
 *   * docker://HOST:PORT
 *   * docker:///var/run/docker.sock
 */
public class MetadataCollectorFactory
{
    List<MetadataCollector> collectors = new ArrayList<MetadataCollector>()
    {{
        add(new DockerMetadataCollector());
    }};

    public MetadataCollector getCollector(URI endpoint) throws MetadataCollectorException
    {
        for (MetadataCollector collector : collectors) {
            if (collector.supports(endpoint)) {
                try {
                    return collector.getClass().getDeclaredConstructor(URI.class).newInstance(endpoint);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                        | InvocationTargetException e) {
                    throw new MetadataCollectorException(e);
                }
            }
        }
        throw new UnkownEndpointException(endpoint);
    }
}
