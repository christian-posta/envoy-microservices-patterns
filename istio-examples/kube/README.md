
Some helper notes:

kubectl exec -it httpbin-client-v1-4233293564-4nlpf  -c http-client  -- sh -c 'export URL_UNDER_TEST=http://httpbin:8080/get export NUM_THREADS=5  && java -jar http-client.jar'



kubectl exec -it httpbin-client-v1-4233293564-4nlpf  -c http-client  -- sh -c 'export URL_UNDER_TEST=http://httpbin:8080/get export NUM_THREADS=5  && java -jar http-client.jar'


kubectl run -i --rm --restart=Never dummy --image=tutum/curl:alpine \
--command -- curl -vvvv http://httpbin:8000/headers



 kubectl run -i --rm --restart=Never gobench --image=piotrminkina/gobench --command -- gobench -u http://httpbin:8080/get -c 3 -r 5
 
 
 istioctl kube-inject -f gobench-deploy.yaml --hub ceposta --tag latest | kubectl create -f -
 
 GOBENCH_POD=$(kubectl get pod | grep gobench | awk '{ print $1 }')
 kubectl exec -it $GOBENCH_POD  -c gobench  -- sh -c 'gobench -u http://httpbin:8080/get -c 3 -r 5'
 kubectl exec -it -c istio-proxy $GOBENCH_POD -- sh -c 'curl http://localhost:15000/stats'
 