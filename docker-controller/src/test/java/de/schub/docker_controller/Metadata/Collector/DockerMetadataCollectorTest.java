package de.schub.docker_controller.Metadata.Collector;


import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.net.URI;

public class DockerMetadataCollectorTest
{
    @Test()
    public void testParseURI() throws Exception
    {
        DockerMetadataCollector collector = new DockerMetadataCollector();
        assertEquals("http://fake-host:2375", collector.parseURI(URI.create("docker://fake-host:2375")).toString());
        assertEquals("unix:///var/run/docker.sock", collector.parseURI(URI.create("docker:///var/run/docker.sock")).toString());
    }

    @Test()
    public void testGet() throws Exception
    {
        String dockerHost = System.getProperty("docker.host");
        System.out.println("DockerHOST:" + dockerHost);
        assumeNotNull(dockerHost);
        DockerMetadataCollector collector = new DockerMetadataCollector(URI.create(dockerHost));
        collector.getAll();
    }
}