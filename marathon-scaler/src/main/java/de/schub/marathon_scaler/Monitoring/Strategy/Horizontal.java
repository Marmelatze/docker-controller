package de.schub.marathon_scaler.Monitoring.Strategy;

import de.schub.marathon_scaler.AppStatistics;
import mesosphere.marathon.client.model.v2.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Dynamically scale a app horizontally (adds more instances)
 * Configuration via marathon labels in the app configuration:
 *
 * ```json
 * "labels": {
 *    "scaling": "horizontal",
 *    "scaling_max_instances": 4,
 *    "scaling_min_instances": 1
 * }
 * ```
 *
 * Will scale up if CPU usage is > 90% or memory usage is > 90%.
 * Will scale down if CPU usage is < 10% or memory usage is < 50%.
 */
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
                    "scaling app '{}' up to {} instances due to cpu usage ({}%)",
                    app.getId(),
                    app.getInstances(),
                    cpuAverage * 100
                );

                return;
            }

            if (memoryAverage > app.getMem() * 0.9) {
                app.setInstances(app.getInstances() + 1);
                logger.info(
                    "scaling app '{}' up to {} instances due to memory usage ({} MB/{} MB)",
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
            if (statistics.getCpu().isPresent() && cpuAverage < 0.1
                && statistics.getMemory().isPresent() && memoryAverage < app.getMem() * 0.5) {
                app.setInstances(app.getInstances() - 1);
                logger.info(
                    "scaling app '{}' down to {} instances due to low resource usage (CPU {}%, Memory: {}/{} MB)",
                    app.getId(),
                    app.getInstances(),
                    cpuAverage * 100,
                    memoryAverage,
                    app.getMem()
                );
            }
        }
    }

    @Override
    public long getStatisticsInterval()
    {
        return 60;
    }
}
