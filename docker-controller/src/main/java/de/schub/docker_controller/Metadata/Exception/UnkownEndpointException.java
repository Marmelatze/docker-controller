package de.schub.docker_controller.Metadata.Exception;

import java.net.URI;

public class UnkownEndpointException extends MetadataCollectorException
{
    public UnkownEndpointException(URI endpoint)
    {
        super("Unkown endpoint '" + endpoint.toString() + "'");
    }
}
