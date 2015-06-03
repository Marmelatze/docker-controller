package de.schub.docker_controller;

import com.ecwid.consul.json.GsonFactory;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.google.gson.JsonSyntaxException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Registry
{
    Logger logger = LoggerFactory.getLogger(Registry.class);
    protected ConsulClient consul;
    protected DockerClient docker;

    public Registry(ConsulClient consul, DockerClient docker)
    {
        this.consul = consul;
        this.docker = docker;
    }

    protected void addContainer(ContainerInfo container)
    {
        logger.info("Adding metadata for container " + container.id());
        ContainerMetadata metadata = ContainerMetadata.createFromContainer(container);
        String json = GsonFactory.getGson().toJson(metadata);
        consul.setKVValue("container/" + container.id(), json);
    }

    protected void removeContainer(String containerId)
    {
    }

    public void sync()
    {
        logger.info("Updating container metadata");
        Map<String, Container> containerNameIndex = new HashMap<>();
        Map<String, Container> containerIndex = new HashMap<>();
        try {
            List<Container> containers = docker.listContainers();
            for (Container container : containers) {
                ContainerInfo info = docker.inspectContainer(container.id());
                this.addContainer(info);
                containerNameIndex.put(container.names().get(0).substring(1), container);
                containerIndex.put(container.id(), container);
            }

        } catch (DockerException | InterruptedException e) {
            e.printStackTrace();
        }

        // remove orphaned services
        logger.info("Removing orpahend services");
        Map<String, Service> services = consul.getAgentServices().getValue();
        for (Map.Entry<String, Service> entry : services.entrySet()) {
            Service service = entry.getValue();
            // get containername from service name node1.mesos-test.local:weave:6783 -> weave
            String[] containerNames = service.getId().split(":");
            if (containerNames.length < 2) {
                continue;
            }
            String containerName = containerNames[1];

            // check if container still exists
            if (containerNameIndex.containsKey(containerName)) {
                continue;
            }
            logger.info("Remove old service " + service.getId());
            consul.agentServiceDeregister(service.getId());
        }

        //remove orphaned metadata
        logger.info("Removing orpahend metadata");
        List<GetValue> metadatas = consul.getKVValues("container").getValue();
        for (GetValue value : metadatas) {
            ContainerMetadata metadata;
            try{
                byte[] bytesEncoded = Base64.getDecoder().decode(value.getValue());
                metadata = GsonFactory.getGson().fromJson(new String(bytesEncoded), ContainerMetadata.class);
            } catch (JsonSyntaxException e) {
                logger.error("Failed to parse json from " + value.getKey());
                continue;
            }
            if (!Objects.equals(metadata.host, DockerController.hostname)) {
                continue;
            }
            if (containerIndex.containsKey(metadata.containerId)) {
                continue;
            }
            logger.info("Remove old metadata " + metadata.containerId);
            consul.deleteKVValue("container/" + metadata.containerId);
        }
        logger.info("finished");
    }
}
