package de.schub.docker_controller;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Parameters(separators = "=")
public class AppParameters
{
    @Parameter(names = {"-collector"}, description = "Metadata Collector")
    public String metadataCollector = "docker://192.168.178.55:2375";

    @Parameter(names = {"-consul"}, description = "Consul Server")
    public String consulServer = "http://localhost:8500";

    @Parameter(names = {"-hostname"}, description = "Hostname to be reported")
    public String hostname;

    @Parameter(names = "--help", help = true)
    public boolean help;

    public AppParameters()
    {
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = "localhost";
        }
    }
}
