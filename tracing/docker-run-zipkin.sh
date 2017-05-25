#!/bin/sh
docker run -itd --name zipkin --expose 9411 -p 9411:9411 openzipkin/zipkin:1.25.0
