
FROM envoyproxy/envoy:v1.7.0

MAINTAINER Christian Posta <christian.posta@gmail.com>

RUN apt-get update && apt-get -q install -y  curl default-jre

WORKDIR /deployments

COPY target/http-client.jar .
COPY entrypoint.sh .

RUN chmod +x entrypoint.sh

ENTRYPOINT [ "./entrypoint.sh" ]