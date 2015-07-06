package de.schub.marathon_scaler.Monitoring.Strategy;

import de.schub.marathon_scaler.AppStatistics;
import de.schub.marathon_scaler.Monitoring.MarathonMonitor;
import mesosphere.marathon.client.model.v2.App;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Optional;

public class HorizontalTest
{
    Horizontal horizontal;
    @Before
    public void setUp()
    {
        horizontal = new Horizontal();
    }

    @Test
    public void testScaleUpByCPU()
    {
        App app = createApp(1, 3);
        AppStatistics statistics = createStatistics(1, 100);
        horizontal.scale(app, statistics);
        assertSame(2, app.getInstances());

        // still 90 percent, but averaged over 2 instnaces
        horizontal.scale(app, statistics);
        assertSame(2, app.getInstances());

        statistics.setCpu(Optional.of(2f));
        horizontal.scale(app, statistics);
        assertSame(3, app.getInstances());

        // reached max
        statistics.setCpu(Optional.of(3f));
        horizontal.scale(app, statistics);
        assertSame("respect max instances", 3, app.getInstances());
    }

    @Test
    public void testScaleDownByCPU()
    {
        App app = createApp(2, 4);
        AppStatistics statistics = createStatistics(0, 100);
        app.setInstances(4);
        horizontal.scale(app, statistics);
        assertSame("scale down by one", 3, app.getInstances());

        horizontal.scale(app, statistics);
        assertSame("scale down by one more", 2, app.getInstances());

        horizontal.scale(app, statistics);
        assertSame("respect min instances", 2, app.getInstances());
    }


    private App createApp(int minInstances, int maxInstances)
    {
        HashMap<String, String> labels = new HashMap<>();
        labels.put(MarathonMonitor.LABEL_SCALING_STRATEGY, Horizontal.NAME);
        labels.put(Horizontal.LABEL_MIN_INSTANCES, Integer.toString(minInstances));
        labels.put(Horizontal.LABEL_MAX_INSTANCES, Integer.toString(maxInstances));

        App app = new App();
        app.setId("/test");
        app.setLabels(labels);
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
    private AppStatistics createStatistics(float cpu, float memory)
    {
        AppStatistics statistics = new AppStatistics();
        statistics.setCpu(Optional.of(cpu));
        statistics.setMemory(Optional.of(memory * 1024 * 1024));

        return statistics;
    }
}
