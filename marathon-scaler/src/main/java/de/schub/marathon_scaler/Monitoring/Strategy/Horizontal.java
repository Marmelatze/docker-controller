package de.schub.marathon_scaler.Monitoring.Strategy;

import de.schub.marathon_scaler.AppStatistics;
import mesosphere.marathon.client.model.v2.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Horizontal implements ScalingStrategy
{
    public static final String NAME = "horizontal";
    public static final String LABEL_MAX_INSTANCES = "scaling_max_instances";
    public static final String LABEL_MIN_INSTANCES = "scaling_min_instances";

    Logger logger = LoggerFactory.getLogger(Horizontal.class);

    @Override
    public void scale(App app, AppStatistics statistics)
    {
        Map<String, String> labels = app.getLabels();
        int maxInstances = app.getInstances();
        if (labels.containsKey(LABEL_MAX_INSTANCES)) {
            maxInstances = Integer.parseInt(labels.get(LABEL_MAX_INSTANCES));
        }
        int minInstances = app.getInstances();
        if (labels.containsKey(LABEL_MIN_INSTANCES)) {
            minInstances = Integer.parseInt(labels.get(LABEL_MIN_INSTANCES));
        }
        if (app.getInstances() == 0) {
            return;
        }

        float cpuAverage = statistics.getCpu().orElse(0F) / app.getInstances();
        float memoryAverage = statistics.getMemoryInMegabytes() / app.getInstances();

        // scaling up
        if (app.getInstances() < maxInstances) {
            if (cpuAverage > 0.9) {
                app.setInstances(app.getInstances() + 1);
                logger.info(
                    "scaling app %s up to %d instances due to cpu usage (%.2f)",
                    app.getId(),
                    app.getInstances(),
                    cpuAverage * 100
                );

                return;
            }

            if (memoryAverage > app.getMem() * 0.9) {
                app.setInstances(app.getInstances() + 1);
                logger.info(
                    "scaling app %s up to %d instances due to memory usage (%.2f MB/$.2f MB)",
                    app.getId(),
                    app.getInstances(),
                    memoryAverage,
                    app.getMem()
                );

                return;
            }
        }


        // scaling down
        if (app.getInstances() > minInstances) {
            // check for presence to avoid scaling down, when no value exists
            if (statistics.getCpu().isPresent() && cpuAverage < 10) {
                app.setInstances(app.getInstances() - 1);
                logger.info(
                    "scaling app %s down to %d instances due to cpu usage (%.2f)",
                    app.getId(),
                    app.getInstances(),
                    cpuAverage * 100
                );

                return;
            }

            if (statistics.getMemory().isPresent() && memoryAverage < app.getMem() * 0.1) {
                app.setInstances(app.getInstances() + 1);
                logger.info(
                    "scaling app %s down to %d instances due to memory usage (%.2f MB/$.2f MB)",
                    app.getId(),
                    app.getInstances(),
                    memoryAverage,
                    app.getMem()
                );

                return;
            }
        }
    }

    @Override
    public double getStatisticsInterval()
    {
        return 60;
    }
}