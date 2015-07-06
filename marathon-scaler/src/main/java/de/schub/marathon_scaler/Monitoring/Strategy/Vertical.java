package de.schub.marathon_scaler.Monitoring.Strategy;

import de.schub.marathon_scaler.AppStatistics;
import mesosphere.marathon.client.model.v2.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Vertical implements ScalingStrategy
{
    public final static String NAME = "vertical";

    Logger logger = LoggerFactory.getLogger(Horizontal.class);


    @Override
    public void scale(App app, AppStatistics statistics)
    {
        // current cpu greater 90 percent
        if (statistics.getCpu().orElse(0F) > 0.9) {
            app.setCpus(app.getCpus() * 1.5);
            logger.info(
                "scaling app %s to %f cpus due to cpu usage (%.2f)",
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
                "scaling app %s to %.2f Memory due to memory usage (%.2f MB/$.2f MB)",
                app.getId(),
                app.getMem(),
                statistics.getMemoryInMegabytes(),
                oldMem
            );
        }

        //if (statistics.getDiskUsage().orElse(0F > app.get))
    }

    @Override
    public double getStatisticsInterval()
    {
        // last 10 minutes
        return 60 * 10;
    }
}
