package de.schub.docker_controller.Metadata.Storage;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Self;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.google.gson.Gson;
import de.schub.docker_controller.Metadata.*;

import javax.inject.Inject;
import java.net.URI;
import java.util.*;
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
        private final ConsulClient consul;

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

        public ConsulMetadataStorage(ConsulClient consul, String prefix, boolean useRemoteServices)
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
            Self value = consul.getAgentSelf().getValue();
            node = new ClusterNode(value.getConfig().getNodeName());
            connected = true;
        }

        /**
         * decode base64 encoded value from consul response
         *
         * @param value
         * @return
         */
        private ContainerMetadata decode(GetValue value)
        {
            byte[] bytesEncoded = Base64.getDecoder().decode(value.getValue());

            ContainerMetadata metadata = gson.fromJson(new String(bytesEncoded), ContainerMetadata.class);
            containerIndex.add(metadata);

            return metadata;
        }

        @Override
        public ContainerMetadata get(String containerId)
        {
            ContainerMetadata metadata = decode(consul.getKVValue(getPrefix() + "/" + containerId).getValue());
            containerIndex.add(metadata);

            return metadata;
        }

        @Override
        public ContainerMetadata get(String nodeId, String containerId)
        {
            return decode(consul.getKVValue(prefix + "/" + nodeId + "/" + containerId).getValue());
        }

        @Override
        public List<ContainerMetadata> getAll()
        {
            List<GetValue> values = consul.getKVValues(getPrefix()).getValue();

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
            consul.setKVValue(getPrefix() + "/" + metadata.containerId, gson.toJson(metadata));
        }

        @Override
        public void set(List<ContainerMetadata> metadatas)
        {
            List<String> containers = consul.getKVKeysOnly(getPrefix() + "/").getValue();
            HashSet<String> addedContainers = new HashSet<>();
            for (ContainerMetadata metadata : metadatas) {
                add(metadata);
                addedContainers.add(metadata.containerId);
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
            consul.deleteKVValue(getPrefix() + "/" + containerId);
        }

        @Override
        public void delete(ContainerMetadata metadata)
        {
            containerIndex.remove(metadata);
            consul.deleteKVValue(getPrefix() + "/" + metadata.containerId);
        }

        @Override
        public Map<String, ContainerService> getServices()
        {
            if (!useRemoteServices) {
                // get services from consul agent
                Map<String, ContainerService> services = new HashMap<>();
                Map<String, Service> value = consul.getAgentServices().getValue();
                for (Map.Entry<String, Service> entry : value.entrySet()) {
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
