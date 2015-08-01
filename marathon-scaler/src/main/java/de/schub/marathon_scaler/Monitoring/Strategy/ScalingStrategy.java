package de.schub.marathon_scaler.Monitoring.Strategy;

import de.schub.marathon_scaler.AppStatistics;
import mesosphere.marathon.client.model.v2.App;

public interface ScalingStrategy
{
    /**
     * perform scaling
     *
     * @param app
     * @param statistics
     */
    public void scale(App app, AppStatistics statistics);

    /**
     * @return time in seconds for statistics interval. e.g. 60 to calculate the stats from the last minute
     */
    public long getStatisticsInterval();
}
