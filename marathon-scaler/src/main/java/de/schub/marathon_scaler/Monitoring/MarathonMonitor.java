package de.schub.marathon_scaler.Monitoring;

import de.schub.marathon_scaler.AppStatistics;
import de.schub.marathon_scaler.Monitoring.Backend.MonitoringBackend;
import de.schub.marathon_scaler.Monitoring.Strategy.ScalingStrategy;
import de.schub.marathon_scaler.Monitoring.Strategy.StrategyFactory;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Get statistics from marathon app with the label "scaling". And scale them with the given strategy.
 * Currenty supported:
 *
 * * horizontal: Add more instances {Horizontal}
 * * vertical: Increase assigned resources {Vertical}
 */
public class MarathonMonitor
{
    public static final String LABEL_SCALING_STRATEGY = "scaling";
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Marathon marathon;
    private final MonitoringBackend monitoringBackend;
    private final StrategyFactory strategyFactory;
    Logger logger = LoggerFactory.getLogger(MarathonMonitor.class);

    public MarathonMonitor(Marathon marathon, MonitoringBackend monitoringBackend, StrategyFactory strategyFactory)
    {
        this.marathon = marathon;
        this.monitoringBackend = monitoringBackend;
        this.strategyFactory = strategyFactory;
    }

    public void run()
    {
        scheduler.scheduleAtFixedRate(
            new Runnable()
            {
                @Override
                public void run()
                {
                    doRun();
                }
            }, 0, 1, TimeUnit.MINUTES
        );

    }

    private void doRun()
    {
        try {
            List<App> apps = marathon.getApps().getApps();
            for (App app : apps) {
                checkApp(app);
            }
        } catch (Exception e) {
            logger.error("Failed to scale apps", e);
        }

        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * get statistics and scale
     * @param app
     */
    private void checkApp(App app)
    {
        if (!app.getLabels().containsKey(LABEL_SCALING_STRATEGY)) {
            return;
        }
        String strategy = app.getLabels().get(LABEL_SCALING_STRATEGY);
        logger.info("watching app {} (strategy {})", app.getId(), strategy);

        ScalingStrategy scalingStrategy;
        try {
            scalingStrategy = strategyFactory.get(strategy);
        } catch (StrategyFactory.UnkownStrategyException e) {
            logger.error(
                String.format("Unkown scaling strategy for app '%s': '%s'", app.getId(), strategy),
                e
            );

            return;
        }
        AppStatistics statistics = monitoringBackend.getStatistics(
            app.getId(),
            scalingStrategy.getStatisticsInterval()
        );

        if (statistics.getCpu().isPresent()) {
            logger.info("CPU usage {}%", Math.round(statistics.getCpu().get() * 100 * 100) / 100);
        }
        if (statistics.getMemory().isPresent()) {
            logger.info("Memory usage {}MB", statistics.getMemoryInMegabytes());
        }
        if (statistics.getDiskUsage().isPresent()) {
            logger.info("Disk usage {}MB", statistics.getDiskUsage().get() / 1024 / 1024);
        }

        scalingStrategy.scale(app, statistics);
        marathon.updateApp(app.getId(), app);


    }

}
