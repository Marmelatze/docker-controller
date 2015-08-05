package de.schub.docker_controller.Metadata;

/**
 * represents a node in the cluster
 */
public class ClusterNode
{
    String name;

    public ClusterNode(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
