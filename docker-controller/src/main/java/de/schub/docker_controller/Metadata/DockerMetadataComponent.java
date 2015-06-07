package de.schub.docker_controller.Metadata;

import dagger.Component;
import de.schub.docker_controller.Metadata.Collector.MetadataCollectorFactory;
import de.schub.docker_controller.Metadata.Storage.MetadataStorageFactory;

@Component(modules = DockerMetadataModule.class)
public interface DockerMetadataComponent
{
    MetadataCollectorFactory getCollectorFactory();

    MetadataStorageFactory getStorageFactory();
}
