# Microservices Patterns with Envoy Proxy
... from this blog: http://blog.christianposta.com


The intention of these demos is to help a reader understand how the Envoy Proxy can be used to implement resilient microservices patterns. Each demo focuses on different Envoy Proxy configurations that can be used to implement the desired behavior.

## Overview

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


Lastly, each demo contains a `http-client.env` file that controls the settings of the http-client we use. Example:

```json
NUM_THREADS=1
DELAY_BETWEEN_CALLS=0
NUM_CALLS_PER_CLIENT=5
URL_UNDER_TEST=http://localhost:15001/get
MIX_RESPONSE_TIMES=false
```

We can control the concurrency with `NUM_THREADS` and the duration with `NUM_CALLS_PER_CLIENT`. For example, in the above configuration, we'll use a single HTTP connection to make five successive calls with no delays between calls (note that `DELAY_BETWEEN_CALLS` is `0`). We can adjust these settings for each of the demos.
