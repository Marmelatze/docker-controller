package de.schub.docker_controller.Metadata.Storage;

import com.google.gson.Gson;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.health.Service;
import de.schub.docker_controller.Metadata.*;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Store collected metadata to consul key value storage
 */
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
         * use services from remote consul nodes
         */
        private final boolean useRemoteServices;
        /**
         * Name of the node we are connected to
         */
        private ClusterNode node;
        /**
         * internal index for mapping container id to metadata
         */
        private ContainerMetadataMap containerIndex = new ContainerMetadataMap();

        /**
         * whether we are connected to the consul agent
         */
        private boolean connected = false;

        /**
         * @param consul            Consul-client
         * @param prefix            prefix for the key-value storage (Default /container)
         * @param useRemoteServices use services from remote consul nodes
         */
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

        /**
         * connect to consul, gets the nodename
         */
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
            ContainerMetadata metadata = decode(
                consul.keyValueClient()
                    .getValueAsString(getPrefix() + "/" + containerId)
                    .get()
            );
            containerIndex.add(metadata);

            return metadata;
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
            if (null == metadata.getClusterNode()) {
                metadata.setClusterNode(getClusterNode());
            }
            containerIndex.add(metadata);
            consul.keyValueClient().putValue(getPrefix() + "/" + metadata.getContainerId(), gson.toJson(metadata));
        }

        @Override
        public void set(List<ContainerMetadata> metadatas)
        {
            List<String> containers = null;
            try {
                containers = consul.keyValueClient().getKeys(getPrefix());
            } catch (NotFoundException ignored) {
            }
            HashSet<String> addedContainers = new HashSet<>();
            for (ContainerMetadata metadata : metadatas) {
                add(metadata);
                addedContainers.add(getPrefix() + "/" + metadata.getContainerId());
            }

            // delete orphaned containers
            if (null == containers || containers.size() == 0) {
                return;
            }
            containers
                .stream()
                .filter(containerId -> !addedContainers.contains(containerId))
                .forEach((containerId1) -> delete(containerId1.replaceFirst(getPrefix(), "")))
            ;
        }

        @Override
        public void delete(ContainerMetadata metadata)
        {
            containerIndex.remove(metadata);
            consul.keyValueClient().deleteKey(getPrefix() + "/" + metadata.getContainerId());
        }

        public void delete(String containerId)
        {
            containerIndex.removeById(containerId);
            consul.keyValueClient().deleteKey(getPrefix() + "/" + containerId);
        }

        @Override
        /**
         * @TODO: This function is more of a hack. It deletes services added by registrator, when the container doesn't exist anymore.
         * Luckily there is a PR to fix this in registrator.
         * Until the PR is merged this function need to exist (https://github.com/gliderlabs/registrator/pull/202),
         * otherwise you will end with plenty of non existing services.
         */
        public Map<String, ContainerService> getServices()
        {
            // get services from consul agent
            if (!useRemoteServices) {
                // init if container index is empty
                if (containerIndex.size() == 0) {
                    this.getAll();
                }
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
                        consul.agentClient().deregister(service.getId());
                        continue;
                    }
                    services.put(metadata.getContainerId(), new ContainerService(service.getId()));
                }

                return services;
            }

            return null;
        }
    }
}
