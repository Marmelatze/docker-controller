package de.schub.docker_controller.Metadata.Storage;

import de.schub.docker_controller.Metadata.ContainerService;

import java.util.List;
import java.util.Map;

public interface ServiceDiscoveryStorage
{
    /**
     * @return all services running in the cluster. Container ID as key
     */
    Map<String, ContainerService> getServices();
}
