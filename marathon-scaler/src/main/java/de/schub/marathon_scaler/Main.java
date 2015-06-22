package de.schub.marathon_scaler;

import de.schub.docker_controller.Metadata.DaggerDockerMetadataComponent;
import de.schub.docker_controller.Metadata.DockerMetadataModule;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;

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
        String endpoint = "http://node01.mesos-cluster.local:8080";
        Marathon marathon = MarathonClient.getInstance(endpoint);

        MarathonScaler marathonScaler = DaggerMarathonScaler.builder()
            .dockerMetadataComponent(
                DaggerDockerMetadataComponent
                    .builder()
                    .dockerMetadataModule(new DockerMetadataModule("adsf"))
                    .build()
            )
            .build();

        marathonScaler.getMarathonMonitor().run();
        marathonScaler.getCustomerService().run();
    }
}
