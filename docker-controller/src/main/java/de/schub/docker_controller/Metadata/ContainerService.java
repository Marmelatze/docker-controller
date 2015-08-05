package de.schub.docker_controller.Metadata;

/**
 * Represents a service (e.g. from consul)
 */
public class ContainerService
{
    private final String id;

    public ContainerService(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }
}
