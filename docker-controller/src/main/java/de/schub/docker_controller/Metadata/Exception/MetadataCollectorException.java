package de.schub.docker_controller.Metadata.Exception;

import java.net.URISyntaxException;

public class MetadataCollectorException extends Exception
{
    public MetadataCollectorException(String message)
    {
        super(message);
    }

    public MetadataCollectorException(Throwable throwable)
    {
        super(throwable);
    }

    public MetadataCollectorException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
