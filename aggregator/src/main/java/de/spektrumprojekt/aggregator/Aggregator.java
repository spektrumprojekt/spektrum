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

import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.chain.AggregatorProxyMessageFeatureCommand;
import de.spektrumprojekt.aggregator.chain.DuplicationDetectionCommand;
import de.spektrumprojekt.aggregator.chain.PublicationDateFilterCommand;
import de.spektrumprojekt.aggregator.chain.SendAggregatorMessageCommand;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.aggregator.duplicate.hashduplicate.HashDuplicationDetection;
import de.spektrumprojekt.aggregator.subscription.PersistentSubscriptionManager;
import de.spektrumprojekt.aggregator.subscription.SubscriptionManager;
import de.spektrumprojekt.aggregator.subscription.handler.CreateSubscriptionMessageHandler;
import de.spektrumprojekt.aggregator.subscription.handler.DeleteSubscriptionMessageHandler;
import de.spektrumprojekt.aggregator.subscription.handler.SynchronizeSubscriptionsMessageHandler;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.communication.MessageHandler;
import de.spektrumprojekt.configuration.Configuration;
import de.spektrumprojekt.i.informationextraction.InformationExtractionCommand;
import de.spektrumprojekt.i.scorer.MessageFeatureContext;
import de.spektrumprojekt.i.scorer.chain.StoreMessageCommand;
import de.spektrumprojekt.persistence.Persistence;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class Aggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Aggregator.class);

    private Communicator communicator = null;

    @SuppressWarnings("rawtypes")
    private final List<MessageHandler> messageHandlers = new ArrayList<MessageHandler>();

    private SubscriptionManager subscriptionManager;

    private final AggregatorConfiguration aggregatorConfiguration;

    private final Persistence persistence;

    private final InformationExtractionCommand<MessageFeatureContext> informationExtractionCommand;

    private AggregatorChain aggregatorChain;

    public Aggregator(Communicator communicator, Persistence persistence,
            AggregatorConfiguration aggregatorConfiguration) {
        this(communicator, persistence, aggregatorConfiguration, null);
    }

    public Aggregator(Communicator communicator, Persistence persistence,
            AggregatorConfiguration aggregatorConfiguration,
            InformationExtractionCommand<MessageFeatureContext> informationExtractionCommand) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (communicator == null) {
            throw new IllegalArgumentException("communicator cannot be null.");
        }
        if (aggregatorConfiguration == null) {
            throw new IllegalArgumentException("aggregatorConfiguration cannot be null.");
        }

        this.persistence = persistence;
        this.communicator = communicator;
        this.aggregatorConfiguration = aggregatorConfiguration;
        this.informationExtractionCommand = informationExtractionCommand;

        this.setupChains();
    }

    public Aggregator(Communicator communicator, Persistence persistence,
            Configuration configuration,
            InformationExtractionCommand<MessageFeatureContext> informationExtractionCommand) {
        this(communicator, persistence, new AggregatorConfiguration(
                configuration), informationExtractionCommand);
    }

    /**
     * loads the configuration and creates a {@link PersistentSubscriptionManager}
     * 
     * @throws ConfigurationException
     *             something is wring with the configuration file
     */
    private void createSubscriptionManager() throws ConfigurationException {

        subscriptionManager = new PersistentSubscriptionManager(communicator,
                persistence, aggregatorChain, aggregatorConfiguration);
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

    public AggregatorChain getAggregatorChain() {
        return aggregatorChain;
    }

    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }

    private void setupChains() {

        aggregatorChain = new AggregatorChain(this.persistence);

        aggregatorChain.getNewMessageChain().addCommand(
                new PublicationDateFilterCommand(this.aggregatorConfiguration
                        .getMinimumPublicationDate()));
        aggregatorChain.getNewMessageChain().addCommand(
                new DuplicationDetectionCommand(new HashDuplicationDetection(
                        aggregatorConfiguration, persistence)));

        if (this.informationExtractionCommand != null) {
            aggregatorChain.getNewMessageChain().addCommand(
                    new AggregatorProxyMessageFeatureCommand(
                            informationExtractionCommand));
        }
        aggregatorChain.getNewMessageChain().addCommand(new AggregatorProxyMessageFeatureCommand(
                new StoreMessageCommand(persistence)));

        SendAggregatorMessageCommand sendAggregatorMessageCommand = new SendAggregatorMessageCommand(
                this.communicator);
        aggregatorChain.getNewMessageChain().addCommand(sendAggregatorMessageCommand);

        aggregatorChain.getAddMessageToSubscriptionChain().addCommand(sendAggregatorMessageCommand);
    }

    /**
     * registers the handlers and opens the {@link Communicator}
     * 
     * @throws ConfigurationException
     *             something is wring with the configuration file
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void start() throws ConfigurationException {
        LOGGER.info("Starting Aggregator ...");

        createSubscriptionManager();
        fillMessageHandlers();

        for (MessageHandler handler : messageHandlers) {
            communicator.registerMessageHandler(handler);
            LOGGER.debug("Adding MessageHandler: " + handler);
        }
        communicator.open();
        LOGGER.info("Aggregator started");
    }

    /**
     * unregisters the handlers and closes the {@link Communicator}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void stop() {
        LOGGER.info("Stopping Aggregator ...");
        for (MessageHandler handler : messageHandlers) {
            if (handler != null) {
                communicator.unregisterMessageHandler(handler);
            }
        }
        subscriptionManager.stop();
        communicator.close();
        LOGGER.info("Aggregator stopped");
    }
}
