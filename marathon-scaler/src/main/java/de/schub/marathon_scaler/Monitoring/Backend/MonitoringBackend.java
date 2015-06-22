package de.schub.marathon_scaler.Monitoring.Backend;

import de.schub.marathon_scaler.AppStatistics;

public interface MonitoringBackend
{
    AppStatistics getStatistics(String appId);
}
