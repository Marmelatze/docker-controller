package de.schub.docker_controller;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Parameters(separators = "=")
public class AppParameters
{
    @Parameter(names = {"-collector"}, description = "Metadata Collector")
    public String metadataCollector = "docker://127.0.0.1:2375";

    @Parameter(names = {"-storage"}, description = "Metadata Storage")
    public String metadataStorage = "consul://127.0.0.1:8500";

    @Parameter(names = {"-hostname"}, description = "Hostname to be reported")
    public String hostname;

    @Parameter(names = {"-interval"}, description = "sync interval in Minutes")
    public int interval = 5;

    @Parameter(names = {"-cadvisor"}, description = "URL of cAdvisor")
    public String cadvisor = "http://127.0.0.1:8080";

    @Parameter(names = {"-proxy_port"}, description = "port for cadvisor proxy")
    public int cadvisorProxyPort = 4041;

    @Parameter(names = "--help", help = true, description = "show this help")
    public boolean help;

    public AppParameters()
    {
        // try to get the system hostname
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = "localhost";
        }
    }
}
