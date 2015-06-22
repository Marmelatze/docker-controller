package de.schub.marathon_scaler.Monitoring.Backend.Prometheus.Exception;

public class PrometheusException extends Throwable
{
    public PrometheusException()
    {
    }

    public PrometheusException(String message)
    {
        super(message);
    }

    public PrometheusException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PrometheusException(Throwable cause)
    {
        super(cause);
    }

    public PrometheusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
