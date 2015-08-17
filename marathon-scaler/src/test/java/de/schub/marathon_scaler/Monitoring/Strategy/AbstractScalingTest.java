package de.schub.marathon_scaler.Monitoring.Strategy;

import de.schub.marathon_scaler.AppStatistics;
import mesosphere.marathon.client.model.v2.App;

import java.util.Optional;

abstract public class AbstractScalingTest
{
    /**
     * @return an app with 64MB memory, 0.5 cpus and 1 instance
     */
    protected App createApp()
    {
        App app = new App();
        app.setId("/test");
        app.setMem(64.0);
        app.setCpus(0.5);
        app.setInstances(1);

        return app;
    }

    /**
     * @param cpu current cpu usage in percent (0 to 1)
     * @param memory current memory usage in megabytes
     * @return
     */
    protected AppStatistics createStatistics(float cpu, float memory)
    {
        AppStatistics statistics = new AppStatistics();
        statistics.setCpu(Optional.of(cpu));
        statistics.setMemory(Optional.of(memory * 1024 * 1024));

        return statistics;
    }
}
