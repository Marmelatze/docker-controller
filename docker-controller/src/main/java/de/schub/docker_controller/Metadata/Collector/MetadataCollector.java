package de.schub.docker_controller.Metadata.Collector;

import de.schub.docker_controller.Metadata.ContainerMetadata;
import de.schub.docker_controller.Metadata.Exception.MetadataCollectorException;

import java.net.URI;
import java.util.List;

public interface MetadataCollector
{
    ContainerMetadata get(String containerId) throws MetadataCollectorException;

    List<ContainerMetadata> getAll() throws MetadataCollectorException;
}
