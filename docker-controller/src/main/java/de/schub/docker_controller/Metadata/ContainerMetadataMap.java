package de.schub.docker_controller.Metadata;

import java.util.HashMap;

/**
 * Map for keeping container id, container name and container metadata
 */
public class ContainerMetadataMap
{
    /**
     * mapping id to container metadata
     */
    HashMap<String, ContainerMetadata> idMap = new HashMap<>();

    /**
     * mapping name to container id
     */
    HashMap<String, String> nameMap = new HashMap<>();

    public ContainerMetadataMap()
    {

    }

    private String getName(ClusterNode node, String name)
    {
        return node + "/" + name;
    }

    private String getName(ContainerMetadata metadata)
    {
        return getName(metadata.clusterNode, metadata.name);
    }

    public void add(ContainerMetadata metadata)
    {
        this.idMap.put(metadata.containerId, metadata);
        this.nameMap.put(getName(metadata), metadata.containerId);
    }

    public ContainerMetadata getById(String id)
    {
        return this.idMap.get(id);
    }

    public ContainerMetadata getByName(ClusterNode node, String name)
    {
        String id = this.nameMap.get(getName(node, name));
        if (null == id) {
            return null;
        }
        return this.idMap.get(id);
    }

    public void remove(ContainerMetadata metadata)
    {
        this.idMap.remove(metadata.containerId);
        this.nameMap.remove(getName(metadata));
    }

    public void removeByName(ClusterNode node, String name)
    {
        removeByName(getName(node, name));
    }

    private void removeByName(String name)
    {
        String id = this.nameMap.remove(name);
        if (null != id) {
            this.idMap.remove(id);
        }
    }


    public void removeById(String id)
    {
        ContainerMetadata metadata = this.idMap.remove(id);
        if (null != metadata) {
            this.nameMap.remove(getName(metadata));
        }
    }

    public void removeNode(ClusterNode node)
    {
        this.nameMap.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(node.getName() + "/"))
            .forEach(entry -> removeByName(entry.getKey()));
        ;
    }
}
