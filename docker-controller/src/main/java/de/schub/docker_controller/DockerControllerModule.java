package de.schub.docker_controller;

import dagger.Module;
import dagger.Provides;
import de.schub.docker_controller.Metadata.Collector.MetadataCollectorFactory;
import de.schub.docker_controller.Metadata.Exception.MetadataCollectorException;

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
        MetadataCollectorFactory metadataCollectorFactory
    )
    {
        return new Registry(
            metadataCollectorFactory.getCollector(URI.create(parameters.metadataCollector))
        );
    }
}
