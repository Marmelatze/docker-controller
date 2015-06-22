package de.schub.marathon_scaler;

import dagger.Component;
import de.schub.docker_controller.Metadata.DockerMetadataComponent;
import de.schub.marathon_scaler.Customer.CustomerService;
import de.schub.marathon_scaler.Monitoring.MarathonMonitor;

import javax.inject.Singleton;

@Singleton
@Component(modules = MarathonScalerModule.class, dependencies = {DockerMetadataComponent.class})
public interface MarathonScaler
{
    CustomerService getCustomerService();

    MarathonMonitor getMarathonMonitor();
}
