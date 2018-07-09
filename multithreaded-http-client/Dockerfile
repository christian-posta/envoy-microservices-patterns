
FROM envoyproxy/envoy:61a9741c3ae9b021b035ce810ab64c6a8f68ba18

MAINTAINER Christian Posta <christian.posta@gmail.com>

RUN apt-get update && apt-get -q install -y  curl default-jre

WORKDIR /deployments

COPY target/http-client.jar .
COPY entrypoint.sh .

RUN chmod +x entrypoint.sh

ENTRYPOINT [ "./entrypoint.sh" ]