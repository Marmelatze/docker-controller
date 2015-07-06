package de.schub.marathon_scaler.Monitoring.Backend.Prometheus.Model;

import java.util.List;

/**
 * a vector can hold many value objects
 */
public class VectorResponse implements PrometheusResponse
{
    private List<Value> value;

    public List<Value> getValue()
    {
        return value;
    }
}
