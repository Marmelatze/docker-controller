package de.schub.docker_controller;

import dagger.Component;
import de.schub.docker_controller.Metadata.DockerMetadataComponent;

import javax.inject.Singleton;

@Singleton
@Component(modules = DockerControllerModule.class,
        dependencies = {DockerMetadataComponent.class}
)
public interface DockerController
{
    Registry getRegistry();
    CadvisorProxy getCadvisorProxy();
}
