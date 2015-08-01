package de.schub.marathon_scaler.Monitoring.Strategy;

import de.schub.marathon_scaler.AppStatistics;
import de.schub.marathon_scaler.Monitoring.MarathonMonitor;
import mesosphere.marathon.client.model.v2.App;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.assertSame;

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

        // 100% across one instance => add one more
        horizontal.scale(app, statistics);
        assertSame(2, app.getInstances());

        // 100% across two instance (50%) => do nothing
        horizontal.scale(app, statistics);
        assertSame(2, app.getInstances());

        // 200% across two instances => add one more
        statistics.setCpu(Optional.of(2f));
        horizontal.scale(app, statistics);
        assertSame(3, app.getInstances());

        // 300% across 3 instances => do nothing, already max instances
        statistics.setCpu(Optional.of(3f));
        horizontal.scale(app, statistics);
        assertSame(3, app.getInstances());
    }

    @Test
    public void testScaleDownByCPU()
    {
        App app = createApp(2, 4);
        AppStatistics statistics = createStatistics(0, 32);
        app.setInstances(4);

        // 0% across 4 instances => scale down by one
        horizontal.scale(app, statistics);
        assertSame(3, app.getInstances());

        // 0% across 3 instances => scale down by one
        horizontal.scale(app, statistics);
        assertSame(2, app.getInstances());

        // 0% across 2 instances => do nothing, already min instances
        horizontal.scale(app, statistics);
        assertSame(2, app.getInstances());
    }

    @Test
    public void testScaleUpByMemory()
    {
        App app = createApp(1, 3);
        AppStatistics statistics = createStatistics(0, 60);
        app.setMem(64.0);

        // 60MB accross one instance => add one instance
        horizontal.scale(app, statistics);
        assertSame(2, app.getInstances());

        // 100MB across two instances => do nothing
        statistics.setMemory(Optional.of(100f * 1024 * 1024));
        horizontal.scale(app, statistics);
        assertSame(2, app.getInstances());

        // 120MB accross two instances => add one instance
        statistics.setMemory(Optional.of(120f * 1024 * 1024));
        horizontal.scale(app, statistics);
        assertSame(3, app.getInstances());

        // 180MB accross three instances => do nothing, already max
        statistics.setMemory(Optional.of(180f * 1024 * 1024));
        horizontal.scale(app, statistics);
        assertSame(3, app.getInstances());
    }

    @Test
    public void testScaleDownByMemory()
    {
        App app = createApp(2, 4);
        AppStatistics statistics = createStatistics(0, 80);
        app.setInstances(4);

        // 80MB across 4 instances => scale down by one
        horizontal.scale(app, statistics);
        assertSame(3, app.getInstances());

        // 80MB across 3 instances => scale down by one
        horizontal.scale(app, statistics);
        assertSame(2, app.getInstances());

        // 80MB across 2 instances => do nothing, already min
        horizontal.scale(app, statistics);
        assertSame(2, app.getInstances());
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
