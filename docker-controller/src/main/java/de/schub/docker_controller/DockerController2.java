package de.schub.docker_controller;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Self;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DockerController2
{
    /*
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static String hostname;
    AppParameters parameters;
    Logger logger = LoggerFactory.getLogger(DockerController2.class);

    public DockerController2(AppParameters parameters)
    {
        this.parameters = parameters;
        start();
    }

    public void start()
    {
        URI consulUri = URI.create(parameters.consulServer);
        ConsulClient consul = new ConsulClient(consulUri.getHost(), consulUri.getPort());
        try {
            Response<Self> agentSelf = consul.getAgentSelf();
            hostname = agentSelf.getValue().getMember().getName();
            logger.info("Connected to consul server " + hostname);
        } catch (Exception e) {
            logger.error("Failed to connect to Consul-Server", e);
            System.exit(1);
        }

        final DockerClient docker = DefaultDockerClient.builder()
                .uri(URI.create(parameters.dockerEndpoint))
                .build();
        try {
            Info dockerInfo = docker.info();
            logger.info("Connected to docker server " + dockerInfo.toString());
        } catch (DockerException | InterruptedException e) {
            logger.error("Failed to connect to Docker-Daemon", e);
            System.exit(1);
        }

        Registry registry = new Registry(consul, docker);
        scheduler.scheduleAtFixedRate(new Runnable()
        {
            @Override
            public void run()
            {
                registry.sync();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }*/
}
