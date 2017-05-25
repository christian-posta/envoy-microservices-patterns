# Microservices Patterns with Envoy Proxy
... from this blog: http://blog.christianposta.com


The intention of these demos is to help a reader understand how the Envoy Proxy can be used to implement resilient microservices patterns. Each demo focuses on different Envoy Proxy configurations that can be used to implement the desired behavior.

## Overview

All of these examples are comprised of a client and a service. The client is a Java http application that simulates making http calls to the "upstream" service (note, we're using [Envoys terminology here, and throught this repo](https://lyft.github.io/envoy/docs/intro/arch_overview/terminology.html)). The client is packaged in a Docker image named `docker.io/ceposta/http-envoy-client:latest`. Alongside the http-client Java application is an instance of [Envoy Proxy](https://lyft.github.io/envoy/docs/intro/what_is_envoy.html). In this deployment model, Envoy is deployed as a [sidecar](http://blog.kubernetes.io/2015/06/the-distributed-system-toolkit-patterns.html) alongside the service (the http client in this case). When the http-client makes outbound calls (to the "upstream" service), all of the calls go through the Envoy Proxy sidecar.

The "upstream" service for these examples is [httpbin.org](http://httpbin.org). httpbin.org allows us to easily simulate HTTP service behavior. It's awesome, so check it out if you've not seen it.


To run the examples, make sure you have access to a docker daemon. If you type:

```bash
docker ps
```

You should see a response without errors.


## Running the demos

To start a demo, run the script (or do it manually) and pass in the parameters for the demo you want to run. Each demo configures the Envoy Proxy differently and may experience different behaviors. 

The format for bootstrapping a demo is:

```bash
./docker-run.sh -d <demo_name>
```

For example, to run the `circuit-breaker` demo:

```bash
./docker-run.sh -d circuit-breaker
```

You can stop the http-client's respective demos with:

```bash
./docker-stop.sh
```

The other various scripts allow us to run the http client (which will be proxied by Envoy):

* `run-http-client.sh` - runs the Java http client using environment variables specified for each demo (in each dir's `http-client.env` file
* `curl.sh` - executes a single `curl` command inside the http-client+envoy container; useful for tests that just need a single (or couple) http calls
* `get-envoy-stat.sh` - queries the Envoy Proxy's admin site for statistics that we can use to interrogate the behavior of the demo and verify it 
* `reset-envoy-stat.sh` - useful for resetting the Envoy Proxy's statistics to re-run some demos/test cases
* `port-forward-minikube.sh` useful if using minikube to expose ports locally on your host
