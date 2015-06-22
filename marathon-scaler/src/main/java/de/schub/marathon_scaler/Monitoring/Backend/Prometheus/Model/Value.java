package de.schub.marathon_scaler.Monitoring.Backend.Prometheus.Model;

import java.util.Map;

public class Value
{
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
