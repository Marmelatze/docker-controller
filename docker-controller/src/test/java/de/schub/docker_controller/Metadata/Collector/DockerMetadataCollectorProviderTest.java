package de.schub.docker_controller.Metadata.Collector;


import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.Info;
import de.schub.docker_controller.Metadata.ContainerMetadata;
import de.schub.docker_controller.Metadata.DockerClientFactory;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNotNull;
import static org.mockito.Mockito.*;


public class DockerMetadataCollectorProviderTest
{
    DockerMetadataCollectorProvider collector;

    @Before
    public void setUp() throws Exception
    {
        collector = new DockerMetadataCollectorProvider(new DockerClientFactory(), "localhost");
    }

    @Test()
    public void testParseURI() throws Exception
    {
        assertEquals("http://fake-host:2375", collector.parseURI(URI.create("docker://fake-host:2375")).toString());
        assertEquals("unix:///var/run/docker.sock", collector.parseURI(URI.create("docker:///var/run/docker.sock")).toString());
    }

    @Test()
    public void testGetWithRealDocker() throws Exception
    {
        String dockerHost = System.getProperty("docker.host");
        assumeNotNull(dockerHost);
        collector.getCollector(URI.create(dockerHost)).getAll();
    }

    @Test()
    public void testGetMetadata() throws Exception
    {
        List<String> env = new ArrayList<>();
        env.add("FOO=BAR");
        env.add("MESOS_TASK_ID=test.1234-foo-bar");
        env.add("MARATHON_APP_ID=/customers/1/foo");
        env.add("MARATHON_APP_VERSION=2015-01-12");

        ContainerInfo containerInfo = mock(ContainerInfo.class, RETURNS_DEEP_STUBS);
        when(containerInfo.id()).thenReturn("123asfd");
        when(containerInfo.name()).thenReturn("/test");
        when(containerInfo.networkSettings().ipAddress()).thenReturn("1.2.3.4");
        when(containerInfo.config().env()).thenReturn(env);

        DockerClient dockerClient = mock(DockerClient.class);
        when(dockerClient.info()).thenReturn(new Info());
        when(dockerClient.inspectContainer("fake_container")).thenReturn(containerInfo);

        DockerClientFactory dockerClientFactory = mock(DockerClientFactory.class);
        when(dockerClientFactory.get(any(URI.class))).thenReturn(dockerClient);

        DockerMetadataCollectorProvider collector = new DockerMetadataCollectorProvider(dockerClientFactory, "localhost");
        ContainerMetadata metadata = collector.getCollector(URI.create("docker://foo:2375")).get("fake_container");
        assertEquals("123asfd", metadata.getContainerId());
        assertEquals("1.2.3.4", metadata.getIp());
        assertEquals("test.1234-foo-bar", metadata.getMesosTaskId());
        assertEquals("/customers/1/foo", metadata.getMarathonAppId());
        assertEquals("2015-01-12", metadata.getMarathonVersion());
    }
}