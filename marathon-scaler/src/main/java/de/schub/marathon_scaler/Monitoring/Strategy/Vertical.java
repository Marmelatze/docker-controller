package de.schub.marathon_scaler.Monitoring.Strategy;

import de.schub.marathon_scaler.AppStatistics;
import mesosphere.marathon.client.model.v2.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamically scale a app verticalle (adds 50% more resources)
 * Configuration via marathon labels in the app configuration:
 *
 * ```json
 * "labels": {
 *    "scaling": "vertical"
 * }
 * ```
 *
 *  Will scale up if CPU usage is > 90% or memory usage is > 90%.
 */
public class Vertical implements ScalingStrategy
{
    public final static String NAME = "vertical";

    Logger logger = LoggerFactory.getLogger(Vertical.class);


    @Override
    public void scale(App app, AppStatistics statistics)
    {
        // current cpu greater 90 percent
        if (statistics.getCpu().orElse(0F) > 0.9) {
            app.setCpus(app.getCpus() * 1.5);
            logger.info(
                "scaling app {} to {} cpus due to cpu usage ({})",
                app.getId(),
                app.getCpus(),
                statistics.getCpu().get() * 100
            );
        }

        // current memory consumption is greater than 90 percent of max
        if (statistics.getMemoryInMegabytes() > app.getMem() * 0.9) {
            Double oldMem = app.getMem();
            app.setMem(oldMem * 1.5);
            logger.info(
                "scaling app {} to {} Memory due to memory usage ({} MB/{} MB)",
                app.getId(),
                app.getMem(),
                statistics.getMemoryInMegabytes(),
                oldMem
            );
        }
    }

    @Override
    public long getStatisticsInterval()
    {
        // last 10 minutes
        return 60 * 10;
    }
}
