package de.schub.docker_controller.Metadata;

import dagger.Component;
import de.schub.docker_controller.Metadata.Collector.MetadataCollectorFactory;

@Component(modules = DockerMetadataModule.class)
public interface DockerMetadataComponent
{
    MetadataCollectorFactory getCollectorFactory();
}
