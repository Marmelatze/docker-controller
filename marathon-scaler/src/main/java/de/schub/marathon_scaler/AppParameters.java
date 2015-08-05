package de.schub.marathon_scaler;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=")
public class AppParameters
{
    @Parameter(names = {"-monitoring"}, description = "Monitoring Backend (supported backends: prometheus)")
    public String monitoringBackend = "prometheus://127.0.0.1:9090";

    @Parameter(names = {"-marathonURI"}, description = "Marathon Server")
    public String marathonURI = "http://127.0.0.1:8080";

    @Parameter(names = {"-consul"}, description = "Consul Server")
    public String consul = "http://127.0.0.1:8500";

    @Parameter(names = "--help", help = true, description = "show this help")
    public boolean help;

    @Override
    public String toString()
    {
        return "AppParameters{" +
               "consul='" + consul + '\'' +
               ", marathonURI='" + marathonURI + '\'' +
               ", monitoringBackend='" + monitoringBackend + '\'' +
               '}';
    }
}
