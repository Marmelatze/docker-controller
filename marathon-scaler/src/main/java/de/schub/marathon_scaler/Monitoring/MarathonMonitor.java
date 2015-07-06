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

public class MarathonMonitor
{
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static final String LABEL_SCALING_STRATEGY = "scaling";
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
        List<App> apps = marathon.getApps().getApps();
        apps.forEach(this::checkApp);

        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkApp(App app)
    {
        if (!app.getLabels().containsKey(LABEL_SCALING_STRATEGY)) {
            return;
        }
        String strategy = app.getLabels().get(LABEL_SCALING_STRATEGY);
        System.out.println(app.getId());
        AppStatistics statistics = monitoringBackend.getStatistics(app.getId());
        System.out.println(statistics);

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

        scalingStrategy.scale(app, statistics);


        if (statistics.getMemory().isPresent()) {
            System.out.println("size in mb " + statistics.getMemory().get() / 1024 / 1024);
        }
        if (statistics.getDiskUsage().isPresent()) {
            System.out.println("disk usage in mb " + statistics.getDiskUsage().get() / 1024 / 1024);
        }

    }

}
