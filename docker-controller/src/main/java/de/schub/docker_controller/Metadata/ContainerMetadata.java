package de.schub.docker_controller.Metadata;

import com.spotify.docker.client.messages.ContainerInfo;

public class ContainerMetadata
{
    public String containerId;

    public String host;

    public String ip;

    public String mesosTaskId;

    public String marathonAppId;
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
               ", host='" + host + '\'' +
               ", ip='" + ip + '\'' +
               ", mesosTaskId='" + mesosTaskId + '\'' +
               ", marathonAppId='" + marathonAppId + '\'' +
               ", marathonVersion='" + marathonVersion + '\'' +
               '}';
    }
}
