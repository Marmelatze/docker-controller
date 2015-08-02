package de.schub.docker_controller.Metadata.Collector;

import de.schub.docker_controller.Metadata.ContainerMetadata;
import de.schub.docker_controller.Metadata.Exception.MetadataCollectorException;

import java.util.List;
import java.util.Map;

public interface MetadataCollector
{
    /**
     * @param containerId
     * @return metadata for the container with the given id
     * @throws MetadataCollectorException
     */
    ContainerMetadata get(String containerId) throws MetadataCollectorException;

    /**
     * @return Metadata for all running containers
     * @throws MetadataCollectorException
     */
    List<ContainerMetadata> getAll() throws MetadataCollectorException;

    /**
     * @return The same as {getAll} but with the container-id as key
     * @throws MetadataCollectorException
     */
    Map<String, ContainerMetadata> getMap() throws MetadataCollectorException;
}
