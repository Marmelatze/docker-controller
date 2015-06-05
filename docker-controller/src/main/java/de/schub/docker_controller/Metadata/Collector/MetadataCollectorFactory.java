package de.schub.docker_controller.Metadata.Collector;

import de.schub.docker_controller.Metadata.Exception.MetadataCollectorException;

import java.net.URI;

public interface MetadataCollectorFactory
{
    MetadataCollector getCollector(URI endpoint);
}
