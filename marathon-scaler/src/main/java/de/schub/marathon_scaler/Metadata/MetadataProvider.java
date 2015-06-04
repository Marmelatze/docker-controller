package de.schub.marathon_scaler.Metadata;

import de.schub.docker_controller.Metadata.ContainerMetadata;
import mesosphere.marathon.client.model.v2.Task;

/**
 * Provides metadata to containers
 */
public interface MetadataProvider
{
    public ContainerMetadata getMetadata(Task task);
}
