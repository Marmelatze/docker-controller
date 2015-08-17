package de.schub.marathon_scaler.Monitoring.Strategy;

import de.schub.marathon_scaler.AppStatistics;
import mesosphere.marathon.client.model.v2.App;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VerticalTest extends AbstractScalingTest
{
    Vertical vertical;
    @Before
    public void setUp()
    {
        vertical = new Vertical();
    }

    @Test
    public void testScaleUpByCPU()
    {
        App app = createApp();
        AppStatistics statistics = createStatistics(1, 32);

        // 100% cpu one instance => increase cpu from 0.5 to 0.75
        vertical.scale(app, statistics);
        assertEquals(0.75, app.getCpus(), 0);

        // still 100% cpu one instance => increase cpu from 0.75 to 0.75
        vertical.scale(app, statistics);
        assertEquals(1.125, app.getCpus(), 0);
    }

    @Test
    public void testScaleDownByCPU()
    {
        App app = createApp();
        AppStatistics statistics = createStatistics(0, 32);

        // 0% cpu => do nothing
        vertical.scale(app, statistics);
        assertEquals(0.5, app.getCpus(), 0);
    }

    @Test
    public void testScaleUpByMemory()
    {
        App app = createApp();
        AppStatistics statistics = createStatistics(0, 60);

        // 60/64MB memory => scale max memory to 96MB
        vertical.scale(app, statistics);
        assertEquals(96, app.getMem(), 0);
    }

    @Test
    public void testScaleDownByMemory()
    {
        App app = createApp();
        AppStatistics statistics = createStatistics(0, 32);

        // 32/64MB memory => do nothing
        vertical.scale(app, statistics);
        assertEquals(64, app.getMem(), 0);
    }

    @Test
    public void testScaleBoth()
    {
        App app = createApp();
        AppStatistics statistics = createStatistics(100, 60);

        // 100% cpu and 60/64MB memory => scale cpu to 0.75 and memory to 96
        vertical.scale(app, statistics);
        assertEquals(0.75, app.getCpus(), 0);
        assertEquals(96, app.getMem(), 0);
    }
}
