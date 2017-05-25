#!/bin/sh
docker exec -it client bash -c 'curl http://localhost:15000/reset_counters'
