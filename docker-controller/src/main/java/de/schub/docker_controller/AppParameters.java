package de.schub.docker_controller;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=")
public class AppParameters
{
    @Parameter(names = {"-collector"}, description = "Metadata Collector")
    public String metadataCollector = "docker://192.168.178.55:2375";

    @Parameter(names = {"-consul"}, description = "Consul Server")
    public String consulServer = "http://localhost:8500";

    @Parameter(names = "--help", help = true)
    public boolean help;
}
