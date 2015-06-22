package de.schub.marathon_scaler.Monitoring.Backend;

import de.schub.marathon_scaler.AppStatistics;
import de.schub.marathon_scaler.Monitoring.Backend.Prometheus.PrometheusClient;

import java.net.URI;
import java.util.Optional;

public class PrometheusBackend implements MonitoringBackend
{

    public PrometheusBackend()
    {
    }

    @Override
    public AppStatistics getStatistics(String appId)
    {
        AppStatistics stats = new AppStatistics();
        PrometheusClient client = new PrometheusClient(URI.create("http://node01.mesos-cluster.local:9090"));
        stats.setCpu(getCPUStats(client, appId));
        stats.setMemory(getMemoryStats(client, appId));
        stats.setDiskUsage(getDiskUsage(client, appId));

        return stats;
    }

    private Optional<Float> getCPUStats(PrometheusClient client, String appId)
    {
        String query = "sum(rate(container_cpu_usage_seconds_total{marathon_app=\"%s\"}[1m])) by (marathon_app) * 100";
        return client.querySingleValue(String.format(query, appId));
    }

    private Optional<Float> getMemoryStats(PrometheusClient client, String appId)
    {
        String query = "avg_over_time(container_memory_usage_bytes{marathon_app=\"%s\"}[1m])";
        return client.querySingleValue(String.format(query, appId));
    }

    private Optional<Float> getDiskUsage(PrometheusClient client, String appId)
    {
        String query = "avg_over_time(container_fs_usage_bytes{marathon_app=\"%s\"}[1m])";
        return client.querySingleValue(String.format(query, appId));
    }
}
