# run httpbin
docker run -it --name httpbin --rm docker.io/kennethreitz/httpbin sh -c "gunicorn --access-logfile - -b 0.0.0.0:8080 httpbin:app"


# run httpbin client and envoy
docker run -d --env-file "./circuit-breaker/http-client.env" --link httpbin --name client -p 15001:15001 -v "$(pwd)/circuit-breaker/conf":/etc/envoy ceposta/http-envoy-client:latest


# add in runtime config
docker run -d --env-file "./circuit-breaker/http-client.env"  --name client -p 15001:15001 -v "$(pwd)/circuit-breaker/conf":/etc/envoy -v "$(pwd)/circuit-breaker/runtime:/srv/runtime/current" ceposta/http-envoy-client:latest


# link in runtime config
# ln -snf /srv/runtime/v1 /srv/runtime/current

Relevant docs:

Global values for panic:
https://www.envoyproxy.io/docs/envoy/latest/configuration/cluster_manager/cluster_runtime#core

Config options for outlier detection
https://www.envoyproxy.io/docs/envoy/v1.5.0/api-v1/cluster_manager/cluster_outlier_detection.html#config-cluster-manager-cluster-outlier-detection

Outlier ejection algo
https://www.envoyproxy.io/docs/envoy/v1.5.0/intro/arch_overview/outlier

How to install the runtime configs
https://www.envoyproxy.io/docs/envoy/latest/configuration/runtime.html#config-runtime

Stats from admin showing panic'd calls:
https://www.envoyproxy.io/docs/envoy/latest/configuration/cluster_manager/cluster_stats.html#config-cluster-manager-cluster-stats



# Good load with # of clients:
docker exec -it client bash -c 'NUM_CALLS_PER_CLIENT=1 URL_UNDER_TEST=http://localhost:15001/status/200; java -jar http-client.jar'

docker exec -it client bash -c 'NUM_THREADS=2 URL_UNDER_TEST=http://localhost:15001/status/200; java -jar http-client.jar'  



# How to simulate outlier detection and non-panic'd load balancing:

* Start up httpbin1
docker run -it --name httpbin1 --rm docker.io/kennethreitz/httpbin sh -c "gunicorn --access-logfile - -b 0.0.0.0:8080 httpbin:app"

* Start up httpbin2
docker run -it --name httpbin2 --rm docker.io/kennethreitz/httpbin sh -c "gunicorn --access-logfile - -b 0.0.0.0:8080 httpbin:app"

* Grap the IP addresses of these endpoints (can use `dkip`) and stuff those into the envoy.json
dkip httpbin1
dkip httpbin2

* Start up the Envoy proxy
docker run -d --env-file "./circuit-breaker/http-client.env"  --name client -p 15001:15001 -v "$(pwd)/circuit-breaker/conf":/etc/envoy -v "$(pwd)/circuit-breaker/runtime:/srv/runtime/current" ceposta/http-envoy-client:latest

* Tail the envoy logs to make sure everything configured properly
docker logs -f client

* Tail the outlier detection ejection logs
docker exec -it client tail -f /deployments/log

* Send traffic through the proxy (that generates 500)
docker exec -it client bash -c 'NUM_CALLS_PER_CLIENT=1 URL_UNDER_TEST=http://localhost:15001/status/500; java -jar http-client.jar'  

* Query the stats for panic
docker exec -it client bash -c 'curl -s http://localhost:15000/stats | grep panic'

* Kill of the envoy/httpclient
docker rm -f client



# Shadowing

docker run -d --env-file "./circuit-breaker/http-client.env"  --name client -p 15001:15001 -v "$(pwd)/circuit-breaker/conf":/etc/envoy ceposta/http-envoy-client:latest