This is part of https://github.com/Marmelatze/saltstack-mesos-test

# Overview

This project provides two docker-containers: 

* [marmelatze/docker-controller)[https://registry.hub.docker.com/u/marmelatze/docker-controller/] to export container metadata to a configurable backend (currently only Consul supported)
* [marmelatze/marathon-sclaer)[https://registry.hub.docker.com/u/marmelatze/marathon-scaler/] to gather monitoring data for containerns (via prometheus) and scale marathon apps if needed.


# Docker-Controller

Will sync container metadata from a collector (docker daemon) to a storage backend (consul). 
In addition it cleans up orphaned consul services. This programm was intended to store metadata from multiple docker 
daemons running on a cluster in the consul KV. The most important metadata was the `MARATHON_APP_ID` environment variable.


Run via

```bash
docker-run marmelatze/docker-controller -collector=docker:///var/run/docker.sock -storage=consul://127.0.0.1:8500 -interval=2
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

* `docker://host:port` Will collect metadata from a docker daemon.
* `consul+docker://host:port/[service]` Will collect metadata from multiple docker daemons, whose address is obtained via consul from the given service name. 


Supported Metadata-Storage Plugins:

* `consul://host:port/[prefix]` Will store the metadata in the consul KV store under the given prefix. 

