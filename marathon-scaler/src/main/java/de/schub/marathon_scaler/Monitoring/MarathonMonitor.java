package de.schub.marathon_scaler.Monitoring;

import de.schub.marathon_scaler.AppStatistics;
import de.schub.marathon_scaler.Monitoring.Backend.MonitoringBackend;
import de.schub.marathon_scaler.Monitoring.Backend.PrometheusBackend;
import mesosphere.marathon.client.Marathon;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MarathonMonitor
{
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    MonitoringBackend backend = new PrometheusBackend();

    public MarathonMonitor(Marathon marathon)
    {

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
        AppStatistics statistics = backend.getStatistics("/db/mysql");
        System.out.println(statistics);
        if (statistics.getMemory().isPresent()) {
            System.out.println("size in mb " + statistics.getMemory().get() / 1024 / 1024);
        }
        if (statistics.getMemory().isPresent()) {
            System.out.println("disk usage in mb " + statistics.getDiskUsage().get() / 1024 / 1024);
        }
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
