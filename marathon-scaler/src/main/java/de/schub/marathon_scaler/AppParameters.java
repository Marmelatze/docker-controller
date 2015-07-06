package de.schub.marathon_scaler;

import com.beust.jcommander.Parameter;

public class AppParameters
{
    @Parameter(names = {"-monitoring"}, description = "Monitoring Backend")
    public String monitoringBackend = "prometheus://192.168.178.55:9090";

    @Parameter(names = {"-marathonURI"}, description = "Marathon Server")
    public String marathonURI = "http://192.168.178.55:8080";

    @Parameter(names = {"-consul"}, description = "Consul")
    public String consul = "http://192.168.178.55:8500";

    @Parameter(names = "--help", help = true)
    public boolean help;
}
