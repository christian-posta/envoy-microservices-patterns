#!/bin/sh

demo=
link=
while getopts d:z: opt; do
  case $opt in
  d)
      demo=$OPTARG
      if [[ "$demo" == "tracing" ]]; then
        link="--link zipkin"
      fi
      ;;
  z)
      link="--link zipkin"
      ;;
  *)
      echo >&2 "Invalid argument: $1"
      ;;
  esac
done

shift $((OPTIND - 1))

echo $demo

if [[ "$demo" == "transparent" ]]; then
    docker run -itd --rm --cap-add NET_ADMIN --name client -p 15001:15001 -v "$(pwd)/transparent/conf":/etc/envoy quay.io/emmanuel0/envoy-ubuntu:latest
    docker exec -it -u0 client /etc/envoy/iptables-redirect.sh
elif [[ "$1" == "fg" ]]; then
    docker run -it --rm --env-file "./$demo/http-client.env" $link --name client -p 15001:15001 -v "$(pwd)/$demo/conf":/etc/envoy ceposta/http-envoy-client:latest
else
    docker run -itd --env-file "./$demo/http-client.env" $link --name client -p 15001:15001 -v "$(pwd)/$demo/conf":/etc/envoy ceposta/http-envoy-client:latest
fi