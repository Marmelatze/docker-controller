package de.schub.marathon_scaler;

import de.schub.docker_controller.Metadata.DaggerDockerMetadataComponent;
import de.schub.docker_controller.Metadata.DockerMetadataModule;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.Task;

import java.util.Collection;
import java.util.List;

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
        String endpoint = "http://10.90.43.79:8080";
        Marathon marathon = MarathonClient.getInstance(endpoint);
        List<App> apps = marathon.getApps().getApps();

        MarathonScaler marathonScaler = DaggerMarathonScaler.builder()
            .dockerMetadataComponent(
                DaggerDockerMetadataComponent
                    .builder()
                    .dockerMetadataModule(new DockerMetadataModule("asdf"))
                    .build()
            )
            .build();

        for (App app : apps) {
            Collection<Task> tasks = marathon.getAppTasks(app.getId()).getTasks();
            System.out.println(tasks);
        }
    }
}
