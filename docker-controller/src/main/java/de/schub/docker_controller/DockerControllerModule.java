package de.schub.docker_controller;

import dagger.Module;
import dagger.Provides;
import de.schub.docker_controller.Metadata.Collector.MetadataCollectorFactory;
import de.schub.docker_controller.Metadata.Storage.MetadataStorageFactory;

import java.net.URI;

@Module
public class DockerControllerModule
{
    AppParameters parameters;

    public DockerControllerModule(AppParameters parameters)
    {
        this.parameters = parameters;
    }

    @Provides
    AppParameters getAppParameters()
    {
        return parameters;
    }

    @Provides
    Registry getRegistry(
        AppParameters parameters,
        MetadataCollectorFactory metadataCollectorFactory,
        MetadataStorageFactory metadataStorageFactory
    )
    {
        return new Registry(
            metadataCollectorFactory.get(URI.create(parameters.metadataCollector)),
            metadataStorageFactory.get(URI.create(parameters.metadataStorage))
        );
    }
}
