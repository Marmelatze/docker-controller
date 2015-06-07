package de.schub.docker_controller;

import de.schub.docker_controller.Metadata.Collector.MetadataCollector;
import de.schub.docker_controller.Metadata.ContainerMetadata;
import de.schub.docker_controller.Metadata.Exception.MetadataCollectorException;
import de.schub.docker_controller.Metadata.Storage.MetadataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

public class Registry
{
    Logger logger = LoggerFactory.getLogger(Registry.class);

    private final MetadataCollector metadataCollector;
    private final MetadataStorage storage;

    @Inject
    public Registry(MetadataCollector metadataCollector, MetadataStorage storage)
    {
        this.metadataCollector = metadataCollector;
        this.storage = storage;
    }

    public void sync()
    {
        logger.info("Updating container metadata");
        try {
            List<ContainerMetadata> metadatas = metadataCollector.getAll();
            storage.set(metadatas);
        } catch (MetadataCollectorException e) {
            logger.error("Failed to get metadata", e);
        }


    }
    /*
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

    protected void removeContainer(ContainerMetadata metadata)
    {
        logger.info("Remove old metadata " + metadata.containerId);
        consul.deleteKVValue("container/" + metadata.containerId);
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
            if (!Objects.equals(metadata.host, DockerController2.hostname)) {
                continue;
            }
            if (containerIndex.containsKey(metadata.containerId)) {
                continue;
            }
            removeContainer(metadata);
        }
        logger.info("finished");
    }*/
}
