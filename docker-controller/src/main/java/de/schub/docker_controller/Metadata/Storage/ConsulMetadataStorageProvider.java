package de.schub.docker_controller.Metadata.Storage;

import com.google.gson.Gson;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.health.Service;
import de.schub.docker_controller.Metadata.*;

import javax.inject.Inject;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsulMetadataStorageProvider implements MetadataStorageProvider
{
    private final ConsulClientFactory consulClientFactory;
    private final Gson gson;

    @Inject
    public ConsulMetadataStorageProvider(ConsulClientFactory consulClientFactory, Gson gson)
    {
        this.consulClientFactory = consulClientFactory;
        this.gson = gson;
    }

    @Override
    public MetadataStorage get(URI endpoint)
    {
        return new ConsulMetadataStorage(
            consulClientFactory.get(endpoint),
            endpoint.getPath(),
            false
        );
    }

    @Override
    public boolean supports(URI endpoint)
    {
        return endpoint.getScheme().equals("consul");
    }

    class ConsulMetadataStorage implements MetadataStorage, ServiceDiscoveryStorage
    {
        private final Consul consul;

        private final String prefix;

        /**
         * Name of the node we are connected to
         */
        private ClusterNode node;

        /**
         * internal index for mapping container id to metadata
         */
        private ContainerMetadataMap containerIndex = new ContainerMetadataMap();

        /**
         *
         */
        private final boolean useRemoteServices;

        private boolean connected = false;

        public ConsulMetadataStorage(Consul consul, String prefix, boolean useRemoteServices)
        {
            this.consul = consul;
            if (null == prefix || 0 == prefix.length()) {
                prefix = "container";
            }
            this.prefix = prefix;
            this.useRemoteServices = useRemoteServices;
        }

        private String getPrefix()
        {
            return prefix + "/" + getClusterNode().getName();
        }

        private ClusterNode getClusterNode()
        {
            connect();

            return node;
        }

        private void connect()
        {
            if (connected) {
                return;
            }
            node = new ClusterNode(consul.agentClient().getAgent().getConfig().getNodeName());
            connected = true;
        }

        /**
         * decode base64 encoded value from consul response
         *
         * @param value
         * @return
         */
        private ContainerMetadata decode(String value)
        {
            if (null == value) {
                return null;
            }
            ContainerMetadata metadata = gson.fromJson(value, ContainerMetadata.class);
            containerIndex.add(metadata);

            return metadata;
        }

        @Override
        public ContainerMetadata get(String containerId)
        {
            ContainerMetadata metadata = decode(consul.keyValueClient().getValueAsString(getPrefix() + "/" + containerId).get());
            containerIndex.add(metadata);

            return metadata;
        }

        @Override
        public ContainerMetadata get(String nodeId, String containerId)
        {
            return decode(consul.keyValueClient().getValueAsString(prefix + "/" + nodeId + "/" + containerId).get());
        }

        @Override
        public List<ContainerMetadata> getAll()
        {
            List<String> values = consul.keyValueClient().getValuesAsString(getPrefix());

            return values
                .stream()
                .map(this::decode)
                .collect(Collectors.toList());
        }

        @Override
        public void add(ContainerMetadata metadata)
        {
            metadata.clusterNode = getClusterNode();
            metadata.host = getClusterNode().getName();
            containerIndex.add(metadata);
            consul.keyValueClient().putValue(getPrefix() + "/" + metadata.containerId, gson.toJson(metadata));
        }

        @Override
        public void set(List<ContainerMetadata> metadatas)
        {
            List<String> containers = consul.keyValueClient().getKeys(getPrefix());
            HashSet<String> addedContainers = new HashSet<>();
            for (ContainerMetadata metadata : metadatas) {
                add(metadata);
                addedContainers.add(getPrefix() + "/" + metadata.containerId);
            }
            // delete orphaned containers
            if (containers.size() == 0) {
                return;
            }
            containers
                .stream()
                .filter(containerId -> !addedContainers.contains(containerId))
                .forEach(this::delete)
            ;
        }

        public void delete(String containerId)
        {
            containerIndex.removeById(containerId);
            consul.keyValueClient().deleteKey(getPrefix() + "/" + containerId);
        }

        @Override
        public void delete(ContainerMetadata metadata)
        {
            containerIndex.remove(metadata);
            consul.keyValueClient().deleteKey(getPrefix() + "/" + metadata.containerId);
        }

        @Override
        public Map<String, ContainerService> getServices()
        {
            if (!useRemoteServices) {
                // get services from consul agent
                Map<String, ContainerService> services = new HashMap<>();
                Map<String, Service> agentServices = consul.agentClient().getServices();
                for (Map.Entry<String, Service> entry : agentServices.entrySet()) {
                    Service service = entry.getValue();
                    // get container name from service name (HOST:CONTAINER_NAME:PORT)
                    // node1.mesos-test.local:weave:6783 -> weave
                    String[] containerNames = service.getId().split(":");
                    if (containerNames.length < 2) {
                        continue;
                    }
                    String containerName = containerNames[1];
                    ContainerMetadata metadata = containerIndex.getByName(getClusterNode(), containerName);
                    if (null == metadata) {
                        continue;
                    }
                    services.put(metadata.containerId, new ContainerService());
                }

                return services;
            }

            // get services from consul catalog

            return null;
        }
    }
}
