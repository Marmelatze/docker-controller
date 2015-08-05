This is part of https://github.com/Marmelatze/saltstack-mesos-test

# Overview

This project provides two docker-containers: 

* [marmelatze/docker-controller)[https://registry.hub.docker.com/u/marmelatze/docker-controller/] to export container metadata to a configurable backend (currently only Consul supported)
* [marmelatze/marathon-sclaer)[https://registry.hub.docker.com/u/marmelatze/marathon-scaler/] to gather monitoring data for containerns (via prometheus) and scale marathon apps if needed.


# Docker-Controller

Will sync container metadata from a collector (docker daemon) to a storage backend (consul). 
In addition it cleans up orphaned consul services. This programm was intended to store metadata from multiple docker 
daemons running on a cluster in the consul KV. The most important metadata was the `MARATHON_APP_ID` environment variable.


Run via:

```bash
docker run marmelatze/docker-controller -collector=docker:///var/run/docker.sock -storage=consul://127.0.0.1:8500 -interval=2
```

Usage:

```
Options:
    -cadvisor
       URL of cAdvisor
       Default: http://127.0.0.1:8080
    -collector
       Metadata Collector
       Default: docker://127.0.0.1:2375
    -hostname
       Hostname to be reported
       Default: current hostname
    -interval
       sync interval in Minutes
       Default: 5
    -proxy_port
       port for cadvisor proxy
       Default: 4041
    -storage
       Metadata Storage
       Default: consul://127.0.0.1:8500
```

Supported Metadata-Collectors:

* `docker://host:port` or `docker:///var/run/docker.sock` Will collect metadata from a docker daemon.
* `consul+docker://host:port/[service]` Will collect metadata from multiple docker daemons, whose address is obtained via consul from the given service name. 


Supported Metadata-Storage Plugins:

* `consul://host:port/[prefix]` Will store the metadata in the consul KV store under the given prefix. 


## Building the Container-Image:

```
export DOCKER_HOST=http://127.0.0.1:2375
./gradlew :docker-controller:distDocker
```

# Marathon-Scaler

Gets statistics for a marathon app via a monitoring backend (currently only prometheus) and scales an
app when needed.

Run via:

```bash
docker run marmelatze/marathon-scaler -monitoring=http://127.0.0.1: -marathonURI=http://127.0.0.1:8080 -consul=http://127.0.0.1:8500
```

```
Options:
        --help
       show this help
       Default: false
    -consul
       Consul Server
       Default: http://127.0.0.1:8500
    -marathonURI
       Marathon Server
       Default: http://127.0.0.1:8080
    -monitoring
       Monitoring Backend (supported backends: prometheus)
       Default: prometheus://127.0.0.1:9090
```

## Scaling horizontal

Add the following to the labels of the marathon app:

```json
"labels": {
    "scaling": "horizontal",
    "scaling_max_instances": 4",
    "scaling_min_instances": 1
}
```

Will scale up if CPU usage is > 90% or memory usage is > 90%.
Will scale down if CPU usage is < 10% or memory usage is < 50%.

## Scaling vertical

Add the following to the labels of the marathon app:

```json
"labels": {
    "scaling": "vertical"
}
```

Will scale up if CPU usage is > 90% or memory usage is > 90%.

## Building the Container-Image:

```
export DOCKER_HOST=http://127.0.0.1:2375
./gradlew :marathon-scaler:distDocker
```