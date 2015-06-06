package de.schub.docker_controller.Metadata.Storage;

import de.schub.docker_controller.Metadata.ContainerMetadata;

import java.util.List;

public interface MetadataStorage
{
    ContainerMetadata get(String containerId);

    List<ContainerMetadata> getAll();

    void set(ContainerMetadata metadata);

    void delete(ContainerMetadata metadata);

}
