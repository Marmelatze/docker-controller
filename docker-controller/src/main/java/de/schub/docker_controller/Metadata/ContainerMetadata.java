package de.schub.docker_controller.Metadata;

import com.google.gson.annotations.Expose;
import com.spotify.docker.client.messages.ContainerInfo;

public class ContainerMetadata
{
    @Expose
    public String containerId;

    @Expose
    public String name;

    @Expose
    public String host;

    public ClusterNode clusterNode;

    @Expose
    public String ip;

    @Expose
    public String mesosTaskId;

    @Expose
    public String marathonAppId;

    @Expose
    public String marathonVersion;

    public static ContainerMetadata createFromContainer(ContainerInfo container)
    {
        return null;
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
}
