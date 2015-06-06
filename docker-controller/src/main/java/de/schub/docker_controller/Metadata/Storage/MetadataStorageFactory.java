package de.schub.docker_controller.Metadata.Storage;

import java.net.URI;

public interface MetadataStorageFactory
{
    MetadataStorage get(URI endpoint);
}
