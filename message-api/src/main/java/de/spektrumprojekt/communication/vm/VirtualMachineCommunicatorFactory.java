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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;


import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.communication.CommunicatorFactory;
import de.spektrumprojekt.communication.CommunicatorFactoryException;
import de.spektrumprojekt.configuration.Configuration;

/**
 * Configuration is done by two properties: "inUrl=vm://endpoint1" "outUrl=vm://endpoint2"
 * 
 * If two vms are configured with the same endpoints the will be connected.
 * 
 * @author tlu
 * 
 */
public class VirtualMachineCommunicatorFactory implements CommunicatorFactory {

    private Configuration configuration;

    private final Map<String, Queue<CommunicationMessage>> queues = new HashMap<String, Queue<CommunicationMessage>>();

    private URL in;

    private URL out;

    private boolean initalized;

    @Override
    public Communicator createCommunicator() {
        if (!initalized) {
            throw new IllegalStateException("Not yet initalized. Forgot to call #initalize ?");
        }

        Queue<CommunicationMessage> inQueue = getQueue(in.getHost());
        Queue<CommunicationMessage> outQueue = getQueue(out.getHost());

        VirtualMachineCommunicator communicator = new VirtualMachineCommunicator(inQueue, outQueue);
        return communicator;
    }

    private synchronized Queue<CommunicationMessage> getQueue(String endpoint) {
        Queue<CommunicationMessage> queue = queues.get(endpoint);
        if (queue == null) {
            queue = new LinkedBlockingQueue<CommunicationMessage>();
            queues.put(endpoint, queue);
        }
        return queue;
    }

    @Override
    public String getURLHandler() {
        return "vm";
    }

    @Override
    public void initialize(Configuration configuration) throws CommunicatorFactoryException {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuation cannot be null!");
        }
        this.configuration = configuration;

        String inUrl = this.configuration.getStringProperty("inUrl");
        String outUrl = this.configuration.getStringProperty("outUrl");

        if (inUrl == null) {
            throw new CommunicatorFactoryException(
                    "Error inializing, reading configuraion. Invalid url. inUrl is not configured.");
        }
        if (outUrl == null) {
            throw new CommunicatorFactoryException(
                    "Error inializing, reading configuraion. Invalid url. outUrl is not configured.");
        }

        try {
            in = new URL(inUrl);
            out = new URL(outUrl);

        } catch (MalformedURLException e) {
            throw new CommunicatorFactoryException(
                    "Error inializing, reading configuraion. Invalid url.", e);
        }

        if (!this.getURLHandler().equals(in.getProtocol().toLowerCase())) {
            throw new CommunicatorFactoryException(
                    "Protocol of url does not match of communocator factory. protocol is "
                            + in.getProtocol());
        }
        if (!this.getURLHandler().equals(out.getProtocol().toLowerCase())) {
            throw new CommunicatorFactoryException(
                    "Protocol of url does not match of communocator factory. protocol is "
                            + out.getProtocol());
        }

        initalized = true;
    }

}
