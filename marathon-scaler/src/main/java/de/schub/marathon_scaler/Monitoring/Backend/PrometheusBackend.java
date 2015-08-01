package de.schub.marathon_scaler.Monitoring.Backend;

import de.schub.marathon_scaler.AppStatistics;
import de.schub.marathon_scaler.Monitoring.Backend.Prometheus.PrometheusClient;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;
import java.util.Optional;

public class PrometheusBackend implements MonitoringBackend
{
    PrometheusClient client;

    public PrometheusBackend(String endpoint)
    {
        try {
            client = new PrometheusClient(new URIBuilder(endpoint).setScheme("http").build());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AppStatistics getStatistics(String appId, long range)
    {
        AppStatistics stats = new AppStatistics();
        stats.setCpu(getCPUStats(client, appId, range));
        stats.setMemory(getMemoryStats(client, appId, range));
        stats.setDiskUsage(getDiskUsage(client, appId, range));

        return stats;
    }

    private Optional<Float> getCPUStats(PrometheusClient client, String appId, long range)
    {
        String query = "sum(rate(container_cpu_usage_seconds_total{marathon_app_id=\"%s\"}[%ds])) by (marathon_app)";
        return client.querySingleValue(String.format(query, appId, range));
    }

    private Optional<Float> getMemoryStats(PrometheusClient client, String appId, long range)
    {
        String query = "avg_over_time(container_memory_usage_bytes{marathon_app_id=\"%s\"}[%ds])";
        return client.querySingleValue(String.format(query, appId, range));
    }

    private Optional<Float> getDiskUsage(PrometheusClient client, String appId, long range)
    {
        String query = "avg_over_time(container_fs_usage_bytes{marathon_app_id=\"%s\"}[%ds])";
        return client.querySingleValue(String.format(query, appId, range));
    }
}
