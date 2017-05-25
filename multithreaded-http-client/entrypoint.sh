#!/usr/bin/env bash

# start envoy
/usr/local/bin/envoy -c /etc/envoy/envoy.json --service-cluster envoy-demo --service-node http-bin-client
