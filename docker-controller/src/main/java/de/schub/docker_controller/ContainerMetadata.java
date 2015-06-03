package de.schub.docker_controller;

import com.spotify.docker.client.messages.ContainerInfo;

public class ContainerMetadata
{
    protected String containerId;

    protected String host;

    protected String ip;

    protected String mesosTaskId;

    protected String marathonAppId;
    protected String marathonVersion;

    public static ContainerMetadata createFromContainer(ContainerInfo container)
    {
        ContainerMetadata metadata = new ContainerMetadata();
        metadata.containerId = container.id();
        metadata.host = DockerController.hostname;
        metadata.ip = container.networkSettings().ipAddress();

        for (String env : container.config().env()) {
            String[] parts = env.split("=");
            String key = parts[0];
            String value = parts[1];
            switch (key) {
                case "MESOS_TASK_ID":
                    metadata.mesosTaskId = value;
                    break;
                case "MARATHON_APP_ID":
                    metadata.marathonAppId = value;
                    break;
                case "MARATHON_APP_VERSION":
                    metadata.marathonVersion = value;
                    break;
            }
        }

        return metadata;
    }
}
