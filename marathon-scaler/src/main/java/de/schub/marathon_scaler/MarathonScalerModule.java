package de.schub.marathon_scaler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dagger.Module;
import dagger.Provides;
import de.schub.docker_controller.Metadata.ConsulClientFactory;
import de.schub.marathon_scaler.Customer.CustomerService;
import de.schub.marathon_scaler.Customer.CustomerStorage;
import de.schub.marathon_scaler.Monitoring.Backend.MonitoringBackend;
import de.schub.marathon_scaler.Monitoring.Backend.PrometheusBackend;
import de.schub.marathon_scaler.Monitoring.MarathonMonitor;
import de.schub.marathon_scaler.Monitoring.Strategy.Horizontal;
import de.schub.marathon_scaler.Monitoring.Strategy.ScalingStrategy;
import de.schub.marathon_scaler.Monitoring.Strategy.StrategyFactory;
import de.schub.marathon_scaler.Monitoring.Strategy.Vertical;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;

import javax.inject.Singleton;
import java.net.URI;
import java.util.HashMap;

@Module
public class MarathonScalerModule
{
    private final AppParameters parameters;

    public MarathonScalerModule(AppParameters parameters)
    {
        this.parameters = parameters;
    }

    @Provides
    ConsulClientFactory getConsulClientFactory()
    {
        return new ConsulClientFactory();
    }

    @Provides
    CustomerStorage getCustomerStorage(ConsulClientFactory consulClientFactory, Gson gson)
    {
        return new CustomerStorage(consulClientFactory.get(URI.create(parameters.consul)), gson);
    }

    @Provides
    Marathon getMarathon()
    {
        return MarathonClient.getInstance(parameters.marathonURI);
    }

    @Provides
    CustomerService getCustomerService(CustomerStorage storage, Marathon marathon)
    {
        return new CustomerService(storage, marathon);
    }

    @Provides
    MonitoringBackend getMonitoringBackend()
    {
        return new PrometheusBackend(parameters.monitoringBackend);
    }


    @Provides
    Vertical getVerticalScalingStrategy()
    {
        return new Vertical();
    }

    @Provides
    Horizontal getHorizontalScalingStrategy()
    {
        return new Horizontal();
    }

    @Provides
    HashMap<String, ScalingStrategy> getStringScalingStrategyHashMap(Vertical vertical, Horizontal horizontal)
    {
        HashMap<String, ScalingStrategy> strategies = new HashMap<>();
        strategies.put(Vertical.NAME, vertical);
        strategies.put(Horizontal.NAME, horizontal);

        return strategies;
    }

    @Provides
    StrategyFactory getStrategyFactory(HashMap<String, ScalingStrategy> strategies)
    {
        return new StrategyFactory(strategies);
    }

    @Provides
    MarathonMonitor getMarathonMonitor(
        Marathon marathon,
        MonitoringBackend monitoringBackend,
        StrategyFactory strategyFactory)
    {
        return new MarathonMonitor(marathon, monitoringBackend, strategyFactory);
    }

    @Provides
    @Singleton
    Gson getGson()
    {
        GsonBuilder builder = new GsonBuilder();

        return builder.create();
    }

}
