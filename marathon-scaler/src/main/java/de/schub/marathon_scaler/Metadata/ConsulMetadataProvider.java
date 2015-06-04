package de.schub.marathon_scaler.Metadata;

import com.ecwid.consul.v1.ConsulClient;
import de.schub.docker_controller.Metadata.ContainerMetadata;
import mesosphere.marathon.client.model.v2.Task;

public class ConsulMetadataProvider implements MetadataProvider
{
    ConsulClient consulClient;



    @Override
    public ContainerMetadata getMetadata(Task task)
    {
        return null;
    }
}
