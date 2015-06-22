package de.schub.marathon_scaler.Monitoring.Backend.Prometheus.Model;

public class PrometheusError implements PrometheusResponse
{
    private String value;

    public String getError()
    {
        return value;
    }
}
