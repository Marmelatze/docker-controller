package de.schub.docker_controller.Metadata.Storage;

import de.schub.docker_controller.Metadata.Service;

import java.util.List;
import java.util.Map;

public interface ServiceDiscoveryStorage
{
    /**
     * @return services running on this instance
     */
    List<Service> getLocalServices();

    /**
     * @return all services running in the cluster
     */
    Map<String, Service> getAllServices();
}
