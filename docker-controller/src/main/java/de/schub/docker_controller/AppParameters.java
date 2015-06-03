package de.schub.docker_controller;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=")
public class AppParameters
{
    @Parameter(names = {"-docker"}, description = "Docker Endpoint")
    public String dockerEndpoint = "unix:///var/run/docker.sock";

    @Parameter(names = {"-consul"}, description = "Consul Server")
    public String consulServer = "http://localhost:8500";

    @Parameter(names = "--help", help = true)
    public boolean help;
}
