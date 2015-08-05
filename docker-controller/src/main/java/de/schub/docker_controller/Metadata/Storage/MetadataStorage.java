package de.schub.docker_controller.Metadata.Storage;

import de.schub.docker_controller.Metadata.ContainerMetadata;

import java.util.List;

public interface MetadataStorage
{
    /**
     * get metadata from storage by container id
     * @param containerId
     * @return
     */
    ContainerMetadata get(String containerId);

    /**
     * get metadata for all containers
     * @return
     */
    List<ContainerMetadata> getAll();

    /**
     * persist metadata
     * @param metadata
     */
    void add(ContainerMetadata metadata);

    /**
     * override all metadata saved for this node
     * @param metadatas
     */
    void set(List<ContainerMetadata> metadatas);

    /**
     * delete metadata for a single container
     * @param metadata
     */
    void delete(ContainerMetadata metadata);
}
