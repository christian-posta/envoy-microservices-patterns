#!/bin/sh

port=${1:=15001}
minikube ssh -- -vnNTL *:$port:$(minikube ip):$port