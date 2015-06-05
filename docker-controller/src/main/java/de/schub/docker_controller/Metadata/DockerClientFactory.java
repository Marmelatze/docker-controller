package de.schub.docker_controller.Metadata;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;

import java.net.URI;

/**
 * Creates a {@see DockerClient}
 * This class exists mainly for decoupling and testing.
 */
public class DockerClientFactory
{
    public DockerClient get(URI endpoint)
    {
        return DefaultDockerClient
            .builder()
            .uri(endpoint)
            .build()
        ;
    }
}
