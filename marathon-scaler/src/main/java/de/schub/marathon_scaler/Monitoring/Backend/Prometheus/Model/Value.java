package de.schub.marathon_scaler.Monitoring.Backend.Prometheus.Model;

import java.util.Map;

/**
 * single value from a vector
 */
public class Value
{
    /**
     * prometheus labels for this value (e.g. marathon_app_id="/test", id="/docker/123abc")
     */
    Map<String, String> metric;
    float value;
    float timestamp;

    public Map<String, String> getMetric()
    {
        return metric;
    }

    public float getValue()
    {
        return value;
    }

    public float getTimestamp()
    {
        return timestamp;
    }
}
