package de.schub.docker_controller.Metadata.Storage;

import de.schub.docker_controller.Metadata.ContainerMetadata;

import java.util.List;

public interface MetadataStorage
{
    ContainerMetadata get(String containerId);

    ContainerMetadata get(String nodeId, String containerId);

    List<ContainerMetadata> getAll();

    void add(ContainerMetadata metadata);

    /**
     * @param metadatas
     */
    void set(List<ContainerMetadata> metadatas);

    void delete(ContainerMetadata metadata);
}
