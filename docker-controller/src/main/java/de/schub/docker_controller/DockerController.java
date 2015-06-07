package de.schub.docker_controller;

import dagger.Component;
import dagger.Provides;
import de.schub.docker_controller.Metadata.DockerMetadataComponent;

@Component(modules = DockerControllerModule.class,
        dependencies = {DockerMetadataComponent.class}
)
public interface DockerController
{
    Registry getRegistry();
}
