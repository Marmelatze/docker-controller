package de.schub.marathon_scaler.Monitoring.Backend.Prometheus.Exception;

/**
 * Exception thrown when a query failed
 */
public class PrometheusQueryException extends PrometheusException
{
    public PrometheusQueryException(String expression, String error)
    {
        super(String.format("Failed to execute prometheus query '%s': %s", expression, error));
    }
}
