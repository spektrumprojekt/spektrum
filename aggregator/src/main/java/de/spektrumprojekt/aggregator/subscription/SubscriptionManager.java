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

package de.spektrumprojekt.aggregator.subscription;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.aggregator.adapter.AdapterManager;
import de.spektrumprojekt.aggregator.adapter.IAdapter;
import de.spektrumprojekt.aggregator.adapter.IAdapterListener;
import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.communication.transfer.MessageCommunicationMessage;
import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.persistence.Persistence;

/**
 * <p>
 * The {@link SubscriptionManager} is responsible for managing subscriptions by dispatching them to
 * the correct {@link IAdapter} implementations obtained from {@link AdapterManager}. When
 * initialized, already existing, persistent subscriptions are read from the persistence layer.
 * </p>
 * 
 * @author Philipp Katz
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
public class SubscriptionManager implements
        /* IDispatcher, */ISubscriptionManager, IAdapterListener {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(SubscriptionManager.class);

    /** The central registry for all available source adapters. */
    private final AdapterManager adapterManager;

    /** The persistence layer for aggregator data. */
    private final Persistence persistence;

    private final Communicator communicator;

    private final AggregatorConfiguration aggregatorConfiguration;

    /**
     * <p>
     * Initialize a new {@link SubscriptionManager} with the specified queue delivering new
     * subscriptions and the specified server configuration. Upon initialization, persistent
     * subscriptions from the database are added.
     * </p>
     * 
     * @param messageQueue
     *            The message queue where outgoing messages will be put.
     */
    public SubscriptionManager(Communicator communicator,
            Persistence persistence,
            AggregatorChain aggregatorChain,
            AggregatorConfiguration aggregatorConfiguration) {

        Validate.notNull(communicator, "Communicator cannot be null.");
        Validate.notNull(persistence, "persistence cannot be null.");
        Validate.notNull(aggregatorChain, "aggregatorChain cannot be null.");
        Validate.notNull(aggregatorConfiguration,
                "AggregatorConfiguration cannot be null.");

        this.communicator = communicator;
        this.persistence = persistence;
        this.aggregatorConfiguration = aggregatorConfiguration;
        this.adapterManager = new AdapterManager(aggregatorChain,
                aggregatorConfiguration, this);

        addPersistentSubscriptions();

        LOGGER.debug("SubscriptionHandler has been initialized.");
    }

    /**
     * <p>
     * Add persistent subscription specifications to the AdapterManager.
     * </p>
     * 
     * @return
     */
    private void addPersistentSubscriptions() {
        List<SourceStatus> persistentSubscriptions = persistence
                .getAggregationSubscriptions();
        LOGGER.debug("adding {} subscriptions from persistence",
                persistentSubscriptions.size());
        for (SourceStatus subscription : persistentSubscriptions) {
            // LOGGER.warn("To schedule ...: {}", subscription);
            if (subscription.isBlocked()) {
                continue;
            }
            String sourceType = subscription.getSubscription()
                    .getConnectorType();
            IAdapter adapter = adapterManager.getAdapter(sourceType);
            if (adapter == null) {
                LOGGER.warn(
                        "no adapter implementation for sourceType {} available",
                        sourceType);
                continue;
            }
            adapter.addSubscription(subscription);
        }
    }

    /**
     * compares two subscriptions for equality based on the connectortype and the accessparameters
     * 
     * @param subscription1
     *            subscription1
     * @param subscription2
     *            subscription2
     * @return equal
     */
    private boolean equal(Subscription subscription1, Subscription subscription2) {
        if (!subscription1.getConnectorType().equals(
                subscription2.getConnectorType())) {
            return false;
        }
        for (Property property : subscription1.getAccessParameters()) {
            Property property2 = subscription2.getAccessParameter(property
                    .getPropertyKey());
            if (property2 == null
                    || !property.getPropertyValue().equals(
                            property2.getPropertyValue())) {
                return false;
            }
        }
        for (Property property : subscription2.getAccessParameters()) {
            Property property2 = subscription1.getAccessParameter(property
                    .getPropertyKey());
            if (property2 == null
                    || !property.getPropertyValue().equals(
                            property2.getPropertyValue())) {
                return false;
            }
        }
        return true;
    }

    public String getStatusReport() {
        StringBuilder status = new StringBuilder();

        // build table header
        status.append("subscriptionId;sourceType;successfulCheckCount;errorCount;consecutiveErrorCount;lastStatus;lastSuccessfulCheck;lastError;blocked");
        status.append('\n');

        List<SourceStatus> aggregationSubscriptions = persistence
                .getAggregationSubscriptions();
        for (SourceStatus subscriptionStatus : aggregationSubscriptions) {
            Subscription subscription = subscriptionStatus.getSubscription();
            status.append(subscription.getGlobalId()).append(';');
            status.append(subscription.getConnectorType()).append(';');
            status.append(subscriptionStatus.getSuccessfulCheckCount()).append(
                    ';');
            status.append(subscriptionStatus.getErrorCount()).append(';');
            status.append(subscriptionStatus.getConsecutiveErrorCount())
                    .append(';');
            String statusType = "";
            if (subscriptionStatus.getLastStatusType() != null) {
                statusType = subscriptionStatus.getLastStatusType().toString();
            }
            status.append(statusType).append(';');
            String lastSuccessfulCheck = "";
            if (subscriptionStatus.getLastSuccessfulCheck() != null) {
                lastSuccessfulCheck = String.valueOf(subscriptionStatus
                        .getLastSuccessfulCheck().getTime());
            }
            String lastError = "";
            if (subscriptionStatus.getLastError() != null) {
                lastError = String.valueOf(subscriptionStatus.getLastError()
                        .getTime());
            }
            status.append(lastSuccessfulCheck).append(';');
            status.append(lastError).append(';');
            status.append(subscriptionStatus.isBlocked());
            status.append('\n');
        }
        return status.toString();
    }

    @Override
    public void processed(Subscription subscription, StatusType statusType) {
        processed(subscription, statusType, null);
    }

    @Override
    public void processed(Subscription subscription, StatusType statusType,
            Exception exception) {
        String subscriptionId = subscription.getGlobalId();
        SourceStatus aggregationStatus = persistence
                .getAggregationSubscription(subscriptionId);
        aggregationStatus.updateCheck(statusType);
        Integer errors = aggregationStatus.getConsecutiveErrorCount();
        if (errors == aggregatorConfiguration.getErrorsForMessage()) {
            String message = String.format(
                    "Warning: Subscription %s had %d subsequent errors",
                    subscriptionId, errors);
            if (exception != null) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(message);
                stringBuilder.append("\nlast Exception:\n");
                // getMessage will not return enough details, if toString is too loud find the root
                // cause exception (or all causes) and chain their message
                stringBuilder.append(exception.toString());
                message = stringBuilder.toString();
            }
            Message errorMessage = new Message(MessageType.ERROR, statusType,
                    subscriptionId, new Date());
            MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN,
                    message);

            errorMessage.addMessagePart(messagePart);

            // TODO separate error message ?
            MessageCommunicationMessage mcm = new MessageCommunicationMessage(
                    errorMessage);
            communicator.sendMessage(mcm);

        }
        if (errors >= aggregatorConfiguration.getMaxConsecErrors()) {
            // remove subscription from being checked
            aggregationStatus.setBlocked(true);
            String sourceType = subscription.getConnectorType();
            IAdapter adapter = adapterManager.getAdapter(sourceType);
            if (adapter != null) {
                adapter.removeSubscription(subscriptionId);
                LOGGER.info(
                        "subscription {} had too many errors ({}), therefore it is blocked now",
                        errors, subscriptionId);
            } else {
                LOGGER.warn("no adapter found for {}", sourceType);
            }
            String message = String
                    .format("Error: Subscription %s had %d subsequent errors, blocking from further checks",
                            subscriptionId, errors);
            if (exception != null) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(message);
                stringBuilder.append("\nlast Exception:\n");
                stringBuilder.append(exception.getMessage());
                message = stringBuilder.toString();
            }
            Message errorMessage = new Message(MessageType.ERROR, statusType,
                    subscriptionId, new Date());
            MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN,
                    message);
            errorMessage.addMessagePart(messagePart);

            // TODO separate error message ?
            MessageCommunicationMessage mcm = new MessageCommunicationMessage(
                    errorMessage);
            communicator.sendMessage(mcm);
        }
        persistence.updateAggregationSubscription(aggregationStatus);
    }

    /**
     * tells the adapterManager to stop
     */
    public void stop() {
        adapterManager.stop();
    }

    @Override
    public void subscribe(Subscription subscription) {
        LOGGER.debug("handle subscription " + subscription);
        String sourceType = subscription.getConnectorType();
        if (sourceType == null) {
            LOGGER.warn(
                    "sourceType for subscription {} is missing, cannot schedule adapter",
                    subscription);
            return;
        }
        String subscriptionId = subscription.getGlobalId();
        IAdapter adapter = adapterManager.getAdapter(sourceType);
        if (adapter == null) {
            LOGGER.warn(
                    "no adapter implementation for sourceType {} available",
                    sourceType);

            Message errorMessage = new Message(MessageType.ERROR,
                    StatusType.ERROR_NO_ADAPTER, subscriptionId, new Date());
            MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN,
                    "no adapter implementation for sourceType " + sourceType
                            + " available");
            errorMessage.addMessagePart(messagePart);

            // TODO separate error message ?
            MessageCommunicationMessage mcm = new MessageCommunicationMessage(
                    errorMessage);
            communicator.sendMessage(mcm);

            return;
        }
        SourceStatus existingSubscription = persistence
                .getAggregationSubscription(subscriptionId);
        if (existingSubscription != null) {
            LOGGER.debug("subscription with ID {} already exists, replacing",
                    subscriptionId);
            unsubscribe(subscriptionId);
        }
        SourceStatus aggregationSubscription = new SourceStatus(
                subscription);
        aggregationSubscription = persistence
                .saveAggregationSubscription(aggregationSubscription);
        adapter.addSubscription(aggregationSubscription);
    }

    /**
     * synchronizes the subscriptions, old subscriptions which are not contained by
     * currentSubscriptions are deleted, new ones are created, existing ones are updated if
     * necessary
     * 
     * @param currentSubscriptions
     */
    public void synchronizeSubscriptions(List<Subscription> currentSubscriptions) {
        List<String> subscriptionsToRemove = new ArrayList<String>();
        // find Subscriptions to remove
        // search in all adapters
        for (IAdapter adapter : adapterManager.getAllAdapters()) {
            for (String subscriptionGlobalId : adapter
                    .getSubscriptionGlobalIds()) {
                LOGGER.debug("Checking existing subscription {}",
                        subscriptionGlobalId);
                boolean isContained = false;
                for (Subscription subscription : currentSubscriptions) {
                    if (subscriptionGlobalId.equals(subscription.getGlobalId())) {
                        isContained = true;
                        LOGGER.debug("Keeping {}", subscriptionGlobalId);
                    }
                }
                if (!isContained) {
                    subscriptionsToRemove.add(subscriptionGlobalId);
                    LOGGER.debug("Removing {}", subscriptionGlobalId);
                }
            }
        }
        // remove old subscriptions
        for (String subscriptionGlobalId : subscriptionsToRemove) {
            unsubscribe(subscriptionGlobalId);
        }
        // update the current subscriptions if neccessary
        for (Subscription subscription : currentSubscriptions) {
            updateOrCreate(subscription);
        }
    }

    @Override
    public void unsubscribe(String subscriptionId) {
        SourceStatus subscription = persistence
                .getAggregationSubscription(subscriptionId);
        if (subscription == null) {
            LOGGER.warn("no subscription with id {}", subscriptionId);
            return;
        }
        persistence.deleteAggregationSubscription(subscriptionId);

        String conectorType = subscription.getSubscription().getConnectorType();
        if (conectorType == null) {
            LOGGER.warn("no source type specified for subscription with id {}",
                    subscriptionId);
            return;
        }
        IAdapter adapter = adapterManager.getAdapter(conectorType);
        if (adapter == null) {
            LOGGER.warn("no adapter for source type {}", conectorType);
            return;
        }
        adapter.removeSubscription(subscriptionId);
    }

    /**
     * updates a {@link Subscription} if necessary
     * 
     * @param subscription
     *            subscription
     * @return true if it needed to be updated
     */
    public boolean updateOrCreate(Subscription subscription) {
        SourceStatus persistentSubscriptionStatus = persistence
                .getAggregationSubscription(subscription.getGlobalId());
        if (persistentSubscriptionStatus != null) {
            Subscription persistentSubscription = persistentSubscriptionStatus
                    .getSubscription();
            if (equal(persistentSubscription, subscription)
                    && !persistentSubscriptionStatus.isBlocked()) {
                return false;
            }
        }
        LOGGER.debug("updating/creating {}", subscription);
        subscribe(subscription);
        return true;
    }
}
