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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.aggregator.adapter.Adapter;
import de.spektrumprojekt.aggregator.adapter.AdapterListener;
import de.spektrumprojekt.aggregator.adapter.AdapterManager;
import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.chain.AggregatorMessageContext;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.communication.transfer.MessageCommunicationMessage;
import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageFilter;
import de.spektrumprojekt.datamodel.message.MessageFilter.OrderDirection;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.SubscriptionFilter;
import de.spektrumprojekt.datamodel.subscription.SubscriptionMessageFilter;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.persistence.Persistence;

/**
 * <p>
 * The {@link PersistentSubscriptionManager} is responsible for managing subscriptions by
 * dispatching them to the correct {@link IAdapter} implementations obtained from
 * {@link AdapterManager}. When initialized, already existing, persistent subscriptions are read
 * from the persistence layer.
 * </p>
 * 
 * @author Philipp Katz
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
public class PersistentSubscriptionManager implements SubscriptionManager, AdapterListener {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PersistentSubscriptionManager.class);

    /** The central registry for all available source adapters. */
    private final AdapterManager adapterManager;

    /** The persistence layer for aggregator data. */
    private final Persistence persistence;

    private final Communicator communicator;

    private final AggregatorConfiguration aggregatorConfiguration;

    private final AggregatorChain aggregatorChain;

    /**
     * <p>
     * Initialize a new {@link PersistentSubscriptionManager} with the specified queue delivering
     * new subscriptions and the specified server configuration. Upon initialization, persistent
     * subscriptions from the database are added.
     * </p>
     * 
     * @param messageQueue
     *            The message queue where outgoing messages will be put.
     */
    public PersistentSubscriptionManager(Communicator communicator, Persistence persistence,
            AggregatorChain aggregatorChain, AggregatorConfiguration aggregatorConfiguration) {

        Validate.notNull(communicator, "Communicator cannot be null.");
        Validate.notNull(persistence, "persistence cannot be null.");
        Validate.notNull(aggregatorChain, "aggregatorChain cannot be null.");
        Validate.notNull(aggregatorConfiguration, "AggregatorConfiguration cannot be null.");

        this.communicator = communicator;
        this.persistence = persistence;
        this.aggregatorConfiguration = aggregatorConfiguration;
        this.adapterManager = new AdapterManager(aggregatorChain, aggregatorConfiguration, this);
        this.aggregatorChain = aggregatorChain;

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
        List<SourceStatus> persistentSubscriptions = persistence.getSourceStatusList();
        LOGGER.debug("adding {} subscriptions from persistence", persistentSubscriptions.size());

        for (SourceStatus subscription : persistentSubscriptions) {
            // LOGGER.warn("To schedule ...: {}", subscription);
            if (subscription.isBlocked()) {
                continue;
            }
            String sourceType = subscription.getSource().getConnectorType();
            Adapter adapter = adapterManager.getAdapter(sourceType);
            if (adapter == null) {
                LOGGER.warn("no adapter implementation for sourceType {} available", sourceType);
                continue;
            }
            adapter.addSource(subscription);
        }
    }

    private void blockSource(SourceStatus sourceStatus, Integer numErrors) {

        // remove subscription from being checked
        sourceStatus.setBlocked(true);
        String sourceType = sourceStatus.getSource().getConnectorType();
        Adapter adapter = adapterManager.getAdapter(sourceType);
        if (adapter != null) {
            adapter.removeSource(sourceStatus.getSource().getGlobalId());
            LOGGER.info("subscription {} had too many errors ({}), therefore it is blocked now",
                    numErrors, sourceStatus.getSource().getGlobalId());
        } else {
            LOGGER.warn("no adapter found for {}", sourceStatus.getSource().getGlobalId());
        }
    }

    @Override
    public boolean continueSubscription(String subscriptionId) {

        if (subscriptionId == null) {
            throw new IllegalArgumentException("subscriptionId cannot be null.");
        }
        Subscription subscription = this.persistence.getSubscriptionByGlobalId(subscriptionId);
        if (subscription == null) {
            LOGGER.warn("no subscription with id {}", subscriptionId);
            return false;
        }
        if (!subscription.isSuspended()) {
            LOGGER.debug("subscription with id {} already active, no action will be taken.",
                    subscriptionId);
            return false;
        }

        subscription.setSuspended(false);

        this.persistence.updateSubscription(subscription);

        return true;
    }

    private SourceStatus createAndStartSource(Source source) throws AdapterNotFoundException {
        Adapter adapter = adapterManager.getAdapter(source.getConnectorType());
        if (adapter == null) {
            LOGGER.warn("no adapter implementation for connector type {} available",
                    source.getConnectorType());
            throw new AdapterNotFoundException("No adapter for connector="
                    + source.getConnectorType(), source);
        }

        SourceStatus sourceStatus = new SourceStatus(source);
        sourceStatus = persistence.saveSourceStatus(sourceStatus);

        adapter.addSource(sourceStatus);

        return sourceStatus;
    }

    /**
     * compares two subscriptions for equality based on the connectortype and the accessparameters
     * 
     * @param source1
     *            subscription1
     * @param source2
     *            subscription2
     * @return equal
     */
    private boolean equal(Subscription subscription1, Subscription subscription2) {

        for (Property property : subscription1.getSubscriptionParameters()) {
            Property property2 = subscription2.getSubscriptionParameter(property.getPropertyKey());
            if (property2 == null
                    || !property.getPropertyValue().equals(property2.getPropertyValue())) {
                return false;
            }
        }
        for (Property property : subscription2.getSubscriptionParameters()) {
            Property property2 = subscription1.getSubscriptionParameter(property.getPropertyKey());
            if (property2 == null
                    || !property.getPropertyValue().equals(property2.getPropertyValue())) {
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

        List<SourceStatus> allSourceStatusList = persistence.getSourceStatusList();
        for (SourceStatus subscriptionStatus : allSourceStatusList) {
            Source source = subscriptionStatus.getSource();
            status.append(source.getGlobalId()).append(';');
            status.append(source.getConnectorType()).append(';');
            status.append(subscriptionStatus.getSuccessfulCheckCount()).append(';');
            status.append(subscriptionStatus.getErrorCount()).append(';');
            status.append(subscriptionStatus.getConsecutiveErrorCount()).append(';');
            String statusType = "";
            if (subscriptionStatus.getLastStatusType() != null) {
                statusType = subscriptionStatus.getLastStatusType().toString();
            }
            status.append(statusType).append(';');
            String lastSuccessfulCheck = "";
            if (subscriptionStatus.getLastSuccessfulCheck() != null) {
                lastSuccessfulCheck = String.valueOf(subscriptionStatus.getLastSuccessfulCheck()
                        .getTime());
            }
            String lastError = "";
            if (subscriptionStatus.getLastError() != null) {
                lastError = String.valueOf(subscriptionStatus.getLastError().getTime());
            }
            status.append(lastSuccessfulCheck).append(';');
            status.append(lastError).append(';');
            status.append(subscriptionStatus.isBlocked());
            status.append('\n');
        }
        return status.toString();
    }

    @Override
    public Subscription getSubscription(String subscriptionGlobalId) {
        return this.persistence.getSubscriptionByGlobalId(subscriptionGlobalId);
    }

    @Override
    public List<Subscription> getSubscriptions(SubscriptionFilter subscriptionFilter) {
        return this.persistence.getSubscriptions(subscriptionFilter);
    }

    @Override
    public void processed(Source source, StatusType statusType) {
        processed(source, statusType, null);
    }

    @Override
    public void processed(Source source, StatusType statusType, Exception exception) {
        String sourceGlobalId = source.getGlobalId();
        SourceStatus sourceStatus = persistence.getSourceStatusBySourceGlobalId(sourceGlobalId);
        sourceStatus.updateCheck(statusType);
        Integer errors = sourceStatus.getConsecutiveErrorCount();
        if (errors > 0) {
            sourceStatus.setLastAccessMessage(exception.getMessage());
        }
        if (errors == aggregatorConfiguration.getErrorsForMessage()
                && aggregatorConfiguration.getErrorsForMessage() != 0) {
            String message = String.format("Warning: Subscription %s had %d subsequent errors",
                    sourceGlobalId, errors);
            if (exception != null) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(message);
                stringBuilder.append("\nlast Exception:\n");
                // getMessage will not return enough details, if toString is too loud find the root
                // cause exception (or all causes) and chain their message
                stringBuilder.append(exception.toString());
                message = stringBuilder.toString();
            }
            Message errorMessage = new Message(MessageType.ERROR, statusType, sourceGlobalId,
                    new Date());
            MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN, message);

            errorMessage.addMessagePart(messagePart);

            // TODO separate error message ?
            MessageCommunicationMessage mcm = new MessageCommunicationMessage(errorMessage);
            communicator.sendMessage(mcm);

        }
        if (errors >= aggregatorConfiguration.getMaxConsecErrors()
                && aggregatorConfiguration.getMaxConsecErrors() != 0) {
            blockSource(sourceStatus, errors);
            String message = String
                    .format("Error: Subscription %s had %d subsequent errors, blocking from further checks",
                            sourceGlobalId, errors);
            if (exception != null) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(message);
                stringBuilder.append("\nlast Exception:\n");
                stringBuilder.append(exception.getMessage());
                message = stringBuilder.toString();
            }
            Message errorMessage = new Message(MessageType.ERROR, statusType, sourceGlobalId,
                    new Date());
            MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN, message);
            errorMessage.addMessagePart(messagePart);

            // TODO separate error message ?
            MessageCommunicationMessage mcm = new MessageCommunicationMessage(errorMessage);
            communicator.sendMessage(mcm);
        }
        persistence.updateSourceStatus(sourceStatus);
    }

    /**
     * tells the adapterManager to stop
     */
    @Override
    public void stop() {
        adapterManager.stop();
    }

    @Override
    public void subscribe(Subscription subscription) throws AdapterNotFoundException {
        this.subscribe(subscription, null);
    }

    @Override
    public void subscribe(Subscription subscription,
            SubscriptionMessageFilter subscriptionMessageFilter) throws AdapterNotFoundException {
        LOGGER.debug("handle subscription " + subscription);
        if (subscription == null) {
            throw new IllegalArgumentException("subscription cannot be null");
        }
        if (subscription.getSource() == null) {
            throw new IllegalArgumentException("subscription.source cannot be null");
        }
        if (subscriptionMessageFilter == null) {
            subscriptionMessageFilter = SubscriptionMessageFilter.NONE;
        }
        Source source = subscription.getSource();
        String sourceType = source.getConnectorType();
        if (sourceType == null) {
            LOGGER.warn("sourceType for subscription {} is missing, cannot schedule adapter",
                    subscription);
            return;
        }

        Subscription existingSubscription = this.persistence.getSubscriptionByGlobalId(subscription
                .getGlobalId());
        if (existingSubscription != null) {
            this.unsubscribe(subscription.getGlobalId());

        }

        Source existingSource = this.persistence.findSource(source.getConnectorType(),
                source.getAccessParameters());

        SourceStatus sourceStatus;
        if (existingSource != null) {
            // the source already exists, so use it
            subscription.setSource(existingSource);
            this.persistence.storeSubscription(subscription);

            sourceStatus = this.persistence.getSourceStatusBySourceGlobalId(existingSource
                    .getGlobalId());
            if (sourceStatus.isBlocked()) {
                this.unblockSource(sourceStatus);
            }

        } else {
            // everything is new so create subscription with source and a new source status
            subscription = this.persistence.storeSubscription(subscription);

            // create subscription and source and start working it
            sourceStatus = this.createAndStartSource(subscription.getSource());

        }

        // push existing messages to the subscription
        if (existingSource != null && subscriptionMessageFilter != null
                && !SubscriptionMessageFilter.NONE.equals(subscriptionMessageFilter)) {
            MessageFilter messageFilter = new MessageFilter();
            messageFilter.setSourceGlobalId(existingSource.getGlobalId());
            messageFilter.setMessageIdOrderDirection(OrderDirection.ASC);

            List<Message> messages = Collections.emptyList();

            if (subscriptionMessageFilter.getStartDate() != null) {
                messageFilter.setMinPublicationDate(subscriptionMessageFilter.getStartDate());
                messages = this.persistence.getMessages(messageFilter);
            }
            if (messages.size() < subscriptionMessageFilter.getLastXMessages()) {
                messageFilter.setMinPublicationDate(null);
                messageFilter.setLastMessagesCount(subscriptionMessageFilter.getLastXMessages());
                messages = this.persistence.getMessages(messageFilter);
            }

            for (Message message : messages) {

                AggregatorMessageContext aggregatorMessageContext = new AggregatorMessageContext(
                        this.aggregatorChain.getPersistence(), message, subscription.getGlobalId());
                this.aggregatorChain.getAddMessageToSubscriptionChain().process(
                        aggregatorMessageContext);

            }
        }
    }

    @Override
    public boolean suspendSubscription(String subscriptionId) {

        if (subscriptionId == null) {
            throw new IllegalArgumentException("subscriptionId cannot be null.");
        }
        Subscription subscription = this.persistence.getSubscriptionByGlobalId(subscriptionId);
        if (subscription == null) {
            LOGGER.warn("no subscription with id {}", subscriptionId);
            return false;
        }
        if (subscription.isSuspended()) {
            LOGGER.debug("subscription with id {} already suspended, no action will be taken.",
                    subscriptionId);
            return false;
        }

        subscription.setSuspended(true);

        this.persistence.updateSubscription(subscription);

        return true;
    }

    /**
     * synchronizes the subscriptions, old subscriptions which are not contained by
     * currentSubscriptions are deleted, new ones are created, existing ones are updated if
     * necessary
     * 
     * @param currentSubscriptions
     * @throws AdapterNotFoundException
     */
    @Override
    public void synchronizeSubscriptions(List<Subscription> currentSubscriptions)
            throws AdapterNotFoundException {
        List<String> subscriptionsToRemove = new ArrayList<String>();
        // find Subscriptions to remove
        // search in all adapters
        for (Adapter adapter : adapterManager.getAllAdapters()) {
            for (String subscriptionGlobalId : adapter.getSourceGlobalIds()) {
                LOGGER.debug("Checking existing subscription {}", subscriptionGlobalId);
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

    private void unblockSource(SourceStatus sourceStatus) {
        sourceStatus.setBlocked(false);
        this.persistence.updateSourceStatus(sourceStatus);

        Adapter adapter = this.adapterManager.getAdapter(sourceStatus.getSource());
        adapter.removeSource(sourceStatus.getGlobalId());
    }

    @Override
    public void unsubscribe(String subscriptionId) {
        if (subscriptionId == null) {
            throw new IllegalArgumentException("subscriptionId cannot be null.");
        }
        Subscription subscription = persistence.getSubscriptionByGlobalId(subscriptionId);
        if (subscription == null) {
            LOGGER.warn("no subscription with id {}", subscriptionId);
            return;
        }
        Source source = subscription.getSource();

        persistence.deleteSubscription(subscriptionId);

        int numberOfSubscriptionsForSource = persistence
                .getNumberOfSubscriptionsBySourceGlobalId(source.getGlobalId());
        if (numberOfSubscriptionsForSource == 0) {

            String connectorType = subscription.getSource().getConnectorType();
            if (connectorType == null) {
                LOGGER.warn("no source type specified for subscription with id {}", subscriptionId);
                return;
            }
            Adapter adapter = adapterManager.getAdapter(connectorType);
            if (adapter == null) {
                LOGGER.warn("no adapter for source type {}", connectorType);
                return;
            }
            adapter.removeSource(source.getGlobalId());

            this.persistence.deleteSource(source.getGlobalId());

        }
    }

    /**
     * updates a {@link Subscription} if necessary
     * 
     * @param subscription
     *            subscription
     * @return true if it needed to be updated
     * @throws AdapterNotFoundException
     */
    public boolean updateOrCreate(Subscription subscription) throws AdapterNotFoundException {

        Subscription persistentSubscription = persistence.getSubscriptionByGlobalId(subscription
                .getGlobalId());

        // check if the subscription is equal
        if (persistentSubscription != null && equal(subscription, persistentSubscription)) {

            // check if the source is already created
            Source persistedSource = this.persistence.findSource(subscription.getSource()
                    .getConnectorType(), subscription.getSource().getAccessParameters());

            // check if the ids of the sources match
            if (persistedSource != null
                    && subscription.getSource().getGlobalId().equals(persistedSource.getGlobalId())) {

                SourceStatus sourceStatus = this.persistence
                        .getSourceStatusBySourceGlobalId(persistedSource.getGlobalId());

                // finally check if the source is blocked so a subscribe will unblock it
                if (!sourceStatus.isBlocked()) {
                    return false;
                }
            }
        }

        subscribe(subscription);

        return true;
    }
}
