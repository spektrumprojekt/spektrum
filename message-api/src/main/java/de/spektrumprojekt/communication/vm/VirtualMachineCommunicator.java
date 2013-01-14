/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
* 
* http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package de.spektrumprojekt.communication.vm;

import java.util.Queue;

import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.MessageHandlerCommunicator;

public class VirtualMachineCommunicator extends MessageHandlerCommunicator {

    private final static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
            .getLogger(VirtualMachineCommunicator.class);

    private final Queue<CommunicationMessage> outQueue, inQueue;

    private boolean stop = false;

    private Thread listeningThead;

    public VirtualMachineCommunicator(Queue<CommunicationMessage> outQueue,
            Queue<CommunicationMessage> inQueue) {
        if (outQueue == null) {
            throw new IllegalArgumentException("outQueue cannot be null");
        }
        if (inQueue == null) {
            throw new IllegalArgumentException("inQueue cannot be null");
        }
        this.outQueue = outQueue;
        this.inQueue = inQueue;

    }

    /**
     * For graceful ends.
     */
    @Override
    public void close() {
        stop = true;
    }

    private void listen() {
        Runnable delievery = new Runnable() {

            @Override
            public void run() {
                try {
                    while (!stop) {
                        CommunicationMessage message = inQueue.poll();
                        if (message == null) {
                            Thread.sleep(1000);
                            continue;
                        }
                        deliverMessage(message);
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("Got interrupted.", e);

                }
            }
        };
        listeningThead = new Thread(delievery);
        listeningThead.start();
    }

    @Override
    public void open() {
        // if we wanted to stop and thread is still alive, wait.
        if (stop && this.listeningThead != null) {
            int limit = 60;
            while (this.listeningThead.isAlive()) {
                limit--;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
                if (limit == 0) {
                    throw new RuntimeException(
                            "Waited for 60 seconds but thread still not stopping. Failing.");
                }
                LOGGER.debug("Listening thread still alive, waiting for another " + limit
                        + " seconds ...");
            }
        }
        if (!stop && this.listeningThead != null && this.listeningThead.isAlive()) {
            // we thread is still running, and we never wanted to stop => still open.
            LOGGER.debug("Listening thread still open. Do nothing.");
            return;
        }
        stop = false;
        listen();
    }

    @Override
    public void sendMessage(CommunicationMessage message) {
        this.outQueue.add(message);
    }

}
