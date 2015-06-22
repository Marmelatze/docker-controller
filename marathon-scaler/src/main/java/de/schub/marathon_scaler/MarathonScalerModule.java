package de.schub.marathon_scaler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dagger.Module;
import dagger.Provides;
import de.schub.docker_controller.Metadata.ConsulClientFactory;
import de.schub.marathon_scaler.Customer.CustomerService;
import de.schub.marathon_scaler.Customer.CustomerStorage;
import de.schub.marathon_scaler.Monitoring.MarathonMonitor;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;

import javax.inject.Singleton;
import java.net.URI;

@Module
public class MarathonScalerModule
{
    @Provides
    ConsulClientFactory getConsulClientFactory()
    {
        return new ConsulClientFactory();
    }

    @Provides
    CustomerStorage getCustomerStorage(ConsulClientFactory consulClientFactory, Gson gson)
    {
        return new CustomerStorage(consulClientFactory.get(URI.create("http://node01.mesos-cluster.local:8500")), gson);
    }

    @Provides
    Marathon getMarathon()
    {
        return MarathonClient.getInstance("http://node01.mesos-cluster.local:8080");
    }

    @Provides
    CustomerService getCustomerService(CustomerStorage storage, Marathon marathon)
    {
        return new CustomerService(storage, marathon);
    }

    @Provides
    MarathonMonitor getMarathonMonitor(Marathon marathon)
    {
        return new MarathonMonitor(marathon);
    }

    @Provides
    @Singleton
    Gson getGson()
    {
        GsonBuilder builder = new GsonBuilder();

        return builder.create();
    }

}
