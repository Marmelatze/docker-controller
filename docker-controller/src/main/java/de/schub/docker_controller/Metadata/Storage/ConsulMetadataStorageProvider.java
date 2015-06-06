package de.schub.docker_controller.Metadata.Storage;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.google.gson.Gson;
import de.schub.docker_controller.Metadata.ContainerMetadata;
import de.schub.docker_controller.Metadata.Service;

import javax.inject.Inject;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsulMetadataStorageProvider implements MetadataStorageProvider
{
    Gson gson;

    @Inject
    public ConsulMetadataStorageProvider(Gson gson)
    {
        this.gson = gson;
    }

    @Override
    public MetadataStorage get(URI endpoint)
    {
        return new ConsulMetadataStorage(
            getClient(endpoint),
            endpoint.getPath() != null ?  endpoint.getPath() : "containers"
        );
    }

    protected ConsulClient getClient(URI endpoint)
    {
        return new ConsulClient(endpoint.getHost(), endpoint.getPort());
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

        public ConsulMetadataStorage(ConsulClient consul, String prefix)
        {
            this.consul = consul;
            this.prefix = prefix;
        }

        /**
         * decode base64 encoded value from consul response
         * @param value
         * @return
         */
        private ContainerMetadata decode(GetValue value)
        {
            byte[] bytesEncoded = Base64.getDecoder().decode(value.getValue());

            return gson.fromJson(new String(bytesEncoded), ContainerMetadata.class);
        }

        @Override
        public ContainerMetadata get(String containerId)
        {
            return decode(consul.getKVValue(prefix + "/" + containerId).getValue());
        }

        @Override
        public List<ContainerMetadata> getAll()
        {
            List<GetValue> values = consul.getKVValues(prefix).getValue();

            return values.stream().map(this::decode).collect(Collectors.toList());
        }

        @Override
        public void set(ContainerMetadata metadata)
        {
            consul.setKVValue(prefix + "/" + metadata.containerId, gson.toJson(metadata));
        }

        @Override
        public void delete(ContainerMetadata metadata)
        {
            consul.deleteKVValue(prefix + "/" + metadata.containerId);
        }

        private Service createService(List<String> data)
        {

        }

        @Override
        public Map<String, Service> getLocalServices()
        {
            Map<String, com.ecwid.consul.v1.agent.model.Service> value = consul.getAgentServices().getValue();

            return null;
        }

        @Override
        public Map<String, Service> getAllServices()
        {
            Map<String, List<String>> value = consul.getCatalogServices(QueryParams.DEFAULT).getValue();
            List<String> peers = consul.getStatusPeers().getValue();

            return null;
        }
    }
}
