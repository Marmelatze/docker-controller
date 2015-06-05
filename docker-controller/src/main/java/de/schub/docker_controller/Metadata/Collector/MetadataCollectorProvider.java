package de.schub.docker_controller.Metadata.Collector;

import de.schub.docker_controller.Metadata.Exception.MetadataCollectorException;

import java.net.URI;

public interface MetadataCollectorProvider
{
    MetadataCollector getCollector(URI endpoint);

    boolean supports(URI endpoint);
}
