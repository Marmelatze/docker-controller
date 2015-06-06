package de.schub.docker_controller.Metadata.Storage;

import java.net.URI;

public interface MetadataStorageProvider
{
    MetadataStorage get(URI endpoint);

    boolean supports(URI endpoint);
}
