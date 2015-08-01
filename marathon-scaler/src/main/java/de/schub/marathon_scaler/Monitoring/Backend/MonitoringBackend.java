package de.schub.marathon_scaler.Monitoring.Backend;

import de.schub.marathon_scaler.AppStatistics;

public interface MonitoringBackend
{
    /**
     *
     * @param appId id of app, to get statistics for
     * @param range time range for calculation (in seconds)
     * @return
     */
    AppStatistics getStatistics(String appId, long range);
}
