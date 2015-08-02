package de.schub.docker_controller.Metadata.Storage;

import java.net.URI;

public interface MetadataStorageProvider
{
    /**
     * Get MetadataStorage from provider with a configuration URI
     *
     * @param endpoint
     * @return
     */
    MetadataStorage get(URI endpoint);

    /**
     * check if provider supports a URI
     *
     * @param endpoint
     * @return
     */
    boolean supports(URI endpoint);
}
