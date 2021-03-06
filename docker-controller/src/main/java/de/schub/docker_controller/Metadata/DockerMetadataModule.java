package de.schub.docker_controller.Metadata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dagger.Module;
import dagger.Provides;
import de.schub.docker_controller.Metadata.Collector.*;
import de.schub.docker_controller.Metadata.Storage.ConsulMetadataStorageProvider;
import de.schub.docker_controller.Metadata.Storage.DefaultMetadataStorageFactory;
import de.schub.docker_controller.Metadata.Storage.MetadataStorageFactory;
import de.schub.docker_controller.Metadata.Storage.MetadataStorageProvider;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * Dagger Module for DockerMetadata
 */
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
    DockerClientFactory getDockerClientFactory()
    {
        return new DockerClientFactory();
    }

    @Provides
    ConsulClientFactory getConsulClientFactory()
    {
        return new ConsulClientFactory();
    }

    @Provides
    DockerMetadataCollectorProvider getDockerMetadataCollectorProvider(
        DockerClientFactory dockerClientFactory,
        @Named("hostname") String hostname)
    {
        return new DockerMetadataCollectorProvider(dockerClientFactory, hostname);
    }

    ConsulDockerMetadataCollectorProvider getConsulDockerMetadataCollectorProvider(
        ConsulClientFactory consulClientFactory,
        DockerMetadataCollectorProvider dockerMetadataCollectorProvider)
    {
        return new ConsulDockerMetadataCollectorProvider(consulClientFactory, dockerMetadataCollectorProvider);
    }

    @Provides
    List<MetadataCollectorProvider> getMetdataCollectorProviders(
        DockerMetadataCollectorProvider dockerMetadataCollectorProvider,
        ConsulDockerMetadataCollectorProvider consulDockerMetadataCollectorProvider)
    {
        ArrayList<MetadataCollectorProvider> providers = new ArrayList<>();
        providers.add(dockerMetadataCollectorProvider);
        providers.add(consulDockerMetadataCollectorProvider);

        return providers;
    }

    @Provides
    MetadataCollectorFactory getMetadataCollectorFactory(
        List<MetadataCollectorProvider> providers
    )
    {
        return new DefaultMetadataCollectorFactory(providers);
    }

    @Provides
    List<MetadataStorageProvider> getMetadataStorageProviders(
        ConsulMetadataStorageProvider consulMetadataStorageProvider
    )
    {
        ArrayList<MetadataStorageProvider> providers = new ArrayList<>();
        providers.add(consulMetadataStorageProvider);

        return providers;
    }

    @Provides
    MetadataStorageFactory getMetadataStorageFactory(
        List<MetadataStorageProvider> providers
    )
    {
        return new DefaultMetadataStorageFactory(providers);
    }

    @Provides
    Gson getGson()
    {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();

        return builder.create();
    }
}
