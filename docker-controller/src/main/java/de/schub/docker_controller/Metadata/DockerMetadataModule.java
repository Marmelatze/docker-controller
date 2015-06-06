package de.schub.docker_controller.Metadata;

import dagger.Module;
import dagger.Provides;
import de.schub.docker_controller.Metadata.Collector.DefaultMetadataCollectorFactory;
import de.schub.docker_controller.Metadata.Collector.DockerMetadataCollectorProvider;
import de.schub.docker_controller.Metadata.Collector.MetadataCollectorFactory;
import de.schub.docker_controller.Metadata.Collector.MetadataCollectorProvider;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Module
public class DockerMetadataModule
{
    String hostname;

    public DockerMetadataModule(String hostname)
    {
        this.hostname = hostname;
    }

    @Provides
    @Named("hostname")
    String getHostname()
    {
        return hostname;
    }

    @Provides
    @Named("foo")
    String getFoo()
    {
        return "foo";
    }

    @Provides
    DockerClientFactory getDockerClientFactory()
    {
        return new DockerClientFactory();
    }

    @Provides
    DockerMetadataCollectorProvider getDockerMetadataCollectorProvider(
        DockerClientFactory dockerClientFactory,
        @Named("hostname") String hostname)
    {
        return new DockerMetadataCollectorProvider(dockerClientFactory, hostname);
    }

    @Provides
    List<MetadataCollectorProvider> getMetdataCollectorProviders(
        DockerMetadataCollectorProvider dockerMetadataCollectorProvider)
    {
        ArrayList<MetadataCollectorProvider> providers = new ArrayList<>();
        providers.add(dockerMetadataCollectorProvider);

        return providers;
    }

    @Provides
    MetadataCollectorFactory getMetadataCollectorFactory(
        List<MetadataCollectorProvider> providers
    )
    {
        return new DefaultMetadataCollectorFactory(providers);
    }
}
