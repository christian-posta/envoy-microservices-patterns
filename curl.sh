#!/bin/sh

curl_string="curl $@"
docker exec -it client $curl_string