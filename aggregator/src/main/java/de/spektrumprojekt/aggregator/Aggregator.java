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

package de.spektrumprojekt.aggregator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.aggregator.subscription.SubscriptionManager;
import de.spektrumprojekt.aggregator.subscription.handler.CreateSubscriptionMessageHandler;
import de.spektrumprojekt.aggregator.subscription.handler.DeleteSubscriptionMessageHandler;
import de.spektrumprojekt.aggregator.subscription.handler.SynchronizeSubscriptionsMessageHandler;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.communication.MessageHandler;
import de.spektrumprojekt.configuration.Configuration;
import de.spektrumprojekt.persistence.Persistence;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(Aggregator.class);

    private Communicator communicator = null;

    @SuppressWarnings("rawtypes")
    private final List<MessageHandler> messageHandlers = new ArrayList<MessageHandler>();

    private SubscriptionManager subscriptionManager;

    private final AggregatorConfiguration aggregatorConfiguration;

    private final Persistence persistence;

    public Aggregator(Communicator communicator, Persistence persistence,
            AggregatorConfiguration configuration) {
        // TODO check for null
        this.communicator = communicator;
        this.aggregatorConfiguration = configuration;
        this.persistence = persistence;
    }

    public Aggregator(Communicator communicator, Persistence persistence,
            Configuration configuration) {
        this(communicator, persistence, new AggregatorConfiguration(
                configuration));
    }

    /**
     * loads the configuration and creates a {@link SubscriptionManager}
     * 
     * @throws ConfigurationException
     *             something is wring with the configuration file
     */
    private void createSubscriptionManager() throws ConfigurationException {

        subscriptionManager = new SubscriptionManager(communicator,
                persistence, aggregatorConfiguration);
    }

    /**
     * initializes the {@link MessageHandler}s
     */
    private void fillMessageHandlers() {
        messageHandlers.add(new CreateSubscriptionMessageHandler(
                subscriptionManager));
        messageHandlers.add(new DeleteSubscriptionMessageHandler(
                subscriptionManager));
        messageHandlers.add(new SynchronizeSubscriptionsMessageHandler(
                subscriptionManager));
    }

    /**
     * registers the handlers and opens the {@link Communicator}
     * 
     * @throws ConfigurationException
     *             something is wring with the configuration file
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void start() throws ConfigurationException {
        LOG.debug("starting ...");
        createSubscriptionManager();
        fillMessageHandlers();
        for (MessageHandler handler : messageHandlers) {
            communicator.registerMessageHandler(handler);
            LOG.debug("Adding MessageHandler: " + handler);
        }
        communicator.open();
        LOG.debug("...startd");
    }

    /**
     * unregisters the handlers and closes the {@link Communicator}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void stop() {
        LOG.debug("stopping ...");
        for (MessageHandler handler : messageHandlers) {
            if (handler != null) {
                communicator.unregisterMessageHandler(handler);
            }
        }
        subscriptionManager.stop();
        communicator.close();
        LOG.debug("... stopped");
    }
}
