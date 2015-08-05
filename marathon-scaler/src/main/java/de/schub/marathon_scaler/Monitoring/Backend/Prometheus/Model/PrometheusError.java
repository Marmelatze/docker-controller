package de.schub.marathon_scaler.Monitoring.Backend.Prometheus.Model;

/**
 * Error from prometheus
 */
public class PrometheusError implements PrometheusResponse
{
    private String value;

    public String getError()
    {
        return value;
    }
}
