package de.schub.docker_controller.Metadata;

import com.google.gson.annotations.Expose;

/**
 * holds all imporant metadata for a container. Used for serialization and deserialization via GSON
 */
public class ContainerMetadata
{
    @Expose
    private String containerId;

    @Expose
    private String name;

    @Expose
    private String host;

    private ClusterNode clusterNode;

    @Expose
    private String image;

    @Expose
    private String ip;

    @Expose
    private String mesosTaskId;

    @Expose
    private String marathonAppId;

    @Expose
    private String marathonVersion;

    protected ContainerMetadata(ContainerMetadataBuilder builder)
    {
        this.containerId = builder.containerId;
        this.name = builder.name;
        this.host = builder.host;
        this.clusterNode = builder.clusterNode;
        this.image = builder.image;
        this.ip = builder.ip;
        this.mesosTaskId = builder.mesosTaskId;
        this.marathonAppId = builder.marathonAppId;
        this.marathonVersion = builder.marathonVersion;
    }

    public static ContainerMetadataBuilder builder()
    {
        return new ContainerMetadataBuilder();
    }

    public String getContainerId()
    {
        return containerId;
    }

    public String getName()
    {
        return name;
    }

    public String getHost()
    {
        return host;
    }

    public ClusterNode getClusterNode()
    {
        return clusterNode;
    }

    public void setClusterNode(ClusterNode clusterNode)
    {
        this.clusterNode = clusterNode;
        this.host = clusterNode.getName();
    }

    public String getImage()
    {
        return image;
    }

    public String getIp()
    {
        return ip;
    }

    public String getMesosTaskId()
    {
        return mesosTaskId;
    }

    public String getMarathonAppId()
    {
        return marathonAppId;
    }

    public String getMarathonVersion()
    {
        return marathonVersion;
    }

    @Override
    public String toString()
    {
        return "ContainerMetadata{" +
               "containerId='" + containerId + '\'' +
               ", name='" + name + '\'' +
               ", host='" + host + '\'' +
               ", ip='" + ip + '\'' +
               ", mesosTaskId='" + mesosTaskId + '\'' +
               ", marathonAppId='" + marathonAppId + '\'' +
               ", marathonVersion='" + marathonVersion + '\'' +
               '}';
    }

    public static class ContainerMetadataBuilder
    {
        private String containerId;
        private String name;
        private String host;
        private ClusterNode clusterNode;
        private String image;
        private String ip;
        private String mesosTaskId;
        private String marathonAppId;
        private String marathonVersion;

        public ContainerMetadataBuilder setContainerId(String containerId)
        {
            this.containerId = containerId;
            return this;
        }

        public ContainerMetadataBuilder setName(String name)
        {
            this.name = name;
            return this;
        }

        public ContainerMetadataBuilder setHost(String host)
        {
            this.host = host;
            return this;
        }

        public ContainerMetadataBuilder setClusterNode(ClusterNode clusterNode)
        {
            this.clusterNode = clusterNode;
            return this;
        }

        public ContainerMetadataBuilder setIp(String ip)
        {
            this.ip = ip;
            return this;
        }

        public ContainerMetadataBuilder setImage(String image)
        {
            this.image = image;
            return this;
        }

        public ContainerMetadataBuilder setMesosTaskId(String mesosTaskId)
        {
            this.mesosTaskId = mesosTaskId;
            return this;
        }

        public ContainerMetadataBuilder setMarathonAppId(String marathonAppId)
        {
            this.marathonAppId = marathonAppId;
            return this;
        }

        public ContainerMetadataBuilder setMarathonVersion(String marathonVersion)
        {
            this.marathonVersion = marathonVersion;
            return this;
        }

        public ContainerMetadata build()
        {
            return new ContainerMetadata(this);
        }
    }
}
