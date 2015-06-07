package de.schub.docker_controller.Metadata.Collector;

import java.net.URI;

public interface MetadataCollectorFactory
{
    MetadataCollector get(URI endpoint);
}
