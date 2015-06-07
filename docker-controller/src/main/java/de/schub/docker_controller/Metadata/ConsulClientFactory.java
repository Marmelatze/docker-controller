package de.schub.docker_controller.Metadata;

import com.ecwid.consul.v1.ConsulClient;

import java.net.URI;

public class ConsulClientFactory
{
    public ConsulClient get(URI endpoint)
    {
        return new ConsulClient(endpoint.getHost(), endpoint.getPort());
    }
}
