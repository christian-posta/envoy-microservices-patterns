/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.christianposta.blog.envoy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ceposta 
 * <a href="http://christianposta.com/blog>http://christianposta.com/blog</a>.
 */
public class HttpSender {

    public static final String numThreadsEnvVar = "NUM_THREADS";
    public static final int numThreadsDefault = 1;


    public static void main(String[] args) throws ExecutionException, InterruptedException {

        int numThreads = System.getenv().keySet().contains(numThreadsEnvVar) ? Integer.valueOf(System.getenv(numThreadsEnvVar)) :
                numThreadsDefault;
        System.out.println("using num threads: " + numThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        Future[] responses = new Future[numThreads];
        for (int i = 0; i < numThreads; i++) {
            responses[i] = executorService.submit(new SenderThread());
        }

        for (int i = 0; i < numThreads; i++) {
            responses[i].get();
        }

        executorService.shutdown();
    }
}

class SenderThread implements Runnable {

    public static final String delayBetweenCallsEnvVar = "DELAY_BETWEEN_CALLS";
    public static final long delayBetweenCallsDefault = 0L;

    public static final String numCallsPerClientEnvVar = "NUM_CALLS_PER_CLIENT";
    public static final int numCallsPerClientDefault = 5;

    public static final String urlUnderTestEnvVar = "URL_UNDER_TEST";
    public static final String urlUnderTestDefault = "http://localhost:15001/get";

    public static final String mixResponseTimesEnvVar = "MIX_RESPONSE_TIMES";
    public static final boolean mixResponseTimesDefault = false;

    public static final String parallelSendsEnvVar = "PARALLEL_SENDS";
    public static final boolean parallelSendsDefault = false;

    public static final String parallelSendsBatchEnvVar = "PARALLEL_SENDS_BATCH";
    public static final int parallelSendsBatchDefault = 5;

    final int numCalls;

    final CloseableHttpClient client = HttpClients.createDefault();
    String[] responses;
    final AtomicInteger failures = new AtomicInteger(0);
    final long delayBetweenCalls;
    final String url;
    final boolean mixedRespTimes;
    final boolean parallelSends;
    final int parallelSendsBatch;

    public SenderThread() {
        delayBetweenCalls = System.getenv().keySet().contains(delayBetweenCallsEnvVar) ? Integer.valueOf(System.getenv(delayBetweenCallsEnvVar)) :
                delayBetweenCallsDefault;
        numCalls = System.getenv().keySet().contains(numCallsPerClientEnvVar) ? Integer.valueOf(System.getenv(numCallsPerClientEnvVar)) :
                numCallsPerClientDefault;

        // consider removing this if we don't really need to keep track of each response
        responses = new String[numCalls];

        url = System.getenv().keySet().contains(urlUnderTestEnvVar) ? System.getenv(urlUnderTestEnvVar) : urlUnderTestDefault;

        mixedRespTimes = System.getenv().keySet().contains(mixResponseTimesEnvVar) ?
                Boolean.valueOf(System.getenv(mixResponseTimesEnvVar)) : mixResponseTimesDefault;

        parallelSends = System.getenv().keySet().contains(parallelSendsEnvVar) ?
                Boolean.valueOf(System.getenv(parallelSendsEnvVar)) : parallelSendsDefault;
        parallelSendsBatch = System.getenv().keySet().contains(parallelSendsBatchEnvVar) ?
                Integer.valueOf(System.getenv(parallelSendsBatchEnvVar)) : parallelSendsBatchDefault;

    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();

        System.out.println("Starting " + threadName + " with numCalls=" + numCalls +
                " parallelSends=" + parallelSends +
        " delayBetweenCalls="+delayBetweenCalls + " url="+url+" mixedRespTimes="+mixedRespTimes);

        long start = System.currentTimeMillis();


        if (parallelSends) {
            Future[] waitForSends;
            final ExecutorService parallelSender = Executors.newFixedThreadPool(parallelSendsBatch);

            // do in parallel batches
            for (int i = 0; i < numCalls; i += parallelSendsBatch) {
                waitForSends = new Future[parallelSendsBatch];
                for (int j = 0; j < parallelSendsBatch; j++) {
                    final int index = i+j;

                    waitForSends[j] = parallelSender.submit(new Runnable() {
                        @Override
                        public void run() {
                            executeRequest(index, url);
                        }
                    });

                }
                for (int k = 0; k < parallelSendsBatch; k++) {
                    try {
                        waitForSends[k].get();
                    } catch (Exception e){}
                }
                System.out.println("finished batch " + i);
            }

            parallelSender.shutdown();

        }else {
            // do sequential
            for (int i = 0; i < numCalls; i++) {
                delay();
                executeRequest(i, url);
            }
        }

        long duration = System.currentTimeMillis() - start;

        System.out.println(threadName + ": successes=[" + (numCalls-failures.get()) + "], failures=[" + failures + "], duration=[" + duration + "ms]");

        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void executeRequest(int i, String url) {
        HttpGet request;
        HttpContext context = HttpClientContext.create();
        try {
            if (mixedRespTimes) {
                request = new HttpGet("http://localhost:15001/delay/" + randoDelay());
            } else {
                request = new HttpGet(url);
            }
            responses[i] = client.execute(request,  new StringResponseHandler(), context);
            if (responses[i].toLowerCase().contains("unexpected")) {
                failures.incrementAndGet();
            }
        } catch (IOException e) {
            e.printStackTrace();
            responses[i] = "had an error";
            failures.incrementAndGet();
        }
    }

    private int randoDelay() {
        int delay = ThreadLocalRandom.current().nextInt(0, 5);
        System.out.println(Thread.currentThread().getName() + ": using delay of : "  + delay);
        return delay;

    }

    private void delay() {
        if (delayBetweenCalls > 0) {
            try {
                Thread.sleep(delayBetweenCalls);
            } catch (InterruptedException e) {
            }
        }
    }
}

class StringResponseHandler implements ResponseHandler<String> {

    public String handleResponse(HttpResponse response) throws IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 300) {
            HttpEntity entity = response.getEntity();
            try {
                return null == entity ? "" : EntityUtils.toString(entity);
            } catch (ParseException | IOException e) {
                return "Error : "+e.getMessage();
            }
        } else {
            return "Unexpected response status: " + status;
        }
    }
}
