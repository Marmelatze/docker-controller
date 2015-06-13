package de.schub.docker_controller;

import dagger.Module;
import dagger.Provides;
import de.schub.docker_controller.Metadata.Collector.MetadataCollector;
import de.schub.docker_controller.Metadata.Collector.MetadataCollectorFactory;
import de.schub.docker_controller.Metadata.Storage.MetadataStorage;
import de.schub.docker_controller.Metadata.Storage.MetadataStorageFactory;

import javax.inject.Singleton;
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
    @Singleton
    MetadataCollector getMetadataCollector(AppParameters parameters, MetadataCollectorFactory metadataCollectorFactory)
    {
        return metadataCollectorFactory.get(URI.create(parameters.metadataCollector));
    }

    @Provides
    @Singleton
    MetadataStorage getMetadataStorage(AppParameters parameters, MetadataStorageFactory metadataStorageFactory)
    {
        return metadataStorageFactory.get(URI.create(parameters.metadataStorage));
    }

    @Provides
    AppParameters getAppParameters()
    {
        return parameters;
    }

    @Provides
    Registry getRegistry(
        AppParameters parameters,
        MetadataCollector metadataCollector,
        MetadataStorage metadataStorage)
    {
        return new Registry(
            metadataCollector,
            metadataStorage,
            parameters.interval
        );
    }

    @Provides
    CadvisorProxy getCadvisorProxy(AppParameters parameters, MetadataCollector metadataCollector)
    {
        return new CadvisorProxy(metadataCollector, parameters.cadvisorProxyPort, parameters.cadvisor);
    }
}
