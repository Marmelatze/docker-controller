package de.schub.docker_controller.Metadata.Storage;

import javax.inject.Inject;
import java.net.URI;
import java.util.List;

public class DefaultMetadataStorageFactory implements MetadataStorageFactory
{
    List<MetadataStorageProvider> providers;

    @Inject
    public DefaultMetadataStorageFactory(List<MetadataStorageProvider> providers)
    {
        this.providers = providers;
    }

    @Override
    public MetadataStorage get(URI endpoint)
    {
        for (MetadataStorageProvider provider : providers) {
            if (provider.supports(endpoint)) {
                return provider.get(endpoint);
            }
        }

        return null;
    }
}
