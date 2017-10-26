#!/bin/sh

curl_string="curl $@"
docker exec -it -u1001 client $curl_string