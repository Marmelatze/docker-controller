package de.schub.docker_controller.Metadata;

import com.orbitz.consul.Consul;

import java.net.URI;

public class ConsulClientFactory
{
    public Consul get(URI endpoint)
    {
        return Consul.newClient(endpoint.getHost(), endpoint.getPort());
    }
}
