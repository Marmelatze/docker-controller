package de.schub.marathon_scaler;

import com.beust.jcommander.JCommander;
import de.schub.docker_controller.Metadata.DaggerDockerMetadataComponent;
import de.schub.docker_controller.Metadata.DockerMetadataModule;

public class Main
{
    static {
        // add a system property such that Simple Logger will include timestamp
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        // add a system property such that Simple Logger will include timestamp in the given format
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "dd-MM-yy HH:mm:ss");
    }

    public static void main(String[] args)
    {
        AppParameters parameters = new AppParameters();
        JCommander jCommander = new JCommander(parameters, args);
        if (parameters.help) {
            jCommander.usage();
            System.exit(1);
        }
        System.out.println(parameters);

        MarathonScaler marathonScaler = DaggerMarathonScaler.builder()
            .dockerMetadataComponent(
                DaggerDockerMetadataComponent
                    .builder()
                    .dockerMetadataModule(new DockerMetadataModule(""))
                    .build()
            )
            .marathonScalerModule(new MarathonScalerModule(parameters))
            .build();

        marathonScaler.getMarathonMonitor().run();
        marathonScaler.getCustomerService().run();
    }
}
