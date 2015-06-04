package de.schub.docker_controller;

import com.beust.jcommander.JCommander;

public class Main
{
    static {
        // set a system property such that Simple Logger will include timestamp
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        // set a system property such that Simple Logger will include timestamp in the given format
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

        new DockerController(parameters);
    }
}
