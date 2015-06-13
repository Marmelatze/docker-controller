package de.schub.marathon_scaler;

import dagger.Component;
import de.schub.docker_controller.Metadata.DockerMetadataComponent;

@Component(modules = MarathonScalerModule.class, dependencies = {DockerMetadataComponent.class})
public interface MarathonScaler
{
}
