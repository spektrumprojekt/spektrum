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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.aggregator.adapter.AccessParameterValidationException;
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
import de.spektrumprojekt.datamodel.message.MessagePublicationDateComperator;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.source.SourceNotFoundException;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.SubscriptionAlreadyExistsException;
import de.spektrumprojekt.datamodel.subscription.SubscriptionFilter;
import de.spektrumprojekt.datamodel.subscription.SubscriptionMessageFilter;
import de.spektrumprojekt.datamodel.subscription.SubscriptionSourceStatus;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.exceptions.SubscriptionNotFoundException;
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

    /**
     * 
     * Task for pushing messages of existing sources asynchronous to the subscription after adding a
     * new subscription to that source.
     */
    private class PushMessagesTask implements Runnable {
        final Subscription subscription;
        final SubscriptionMessageFilter subscriptionMessageFilter;
        final String sourceGlobalId;

        PushMessagesTask(Subscription subscription,
                SubscriptionMessageFilter subscriptionMessageFilter, String sourceGlobalId) {
            this.subscription = subscription;
            this.subscriptionMessageFilter = subscriptionMessageFilter;
            this.sourceGlobalId = sourceGlobalId;
        }

        @Override
        public void run() {
            LOGGER.debug("Pushing messages of existing source async to new subscription");
            MessageFilter messageFilter = new MessageFilter();
            messageFilter.setSourceGlobalId(this.sourceGlobalId);
            messageFilter.setMessageIdOrderDirection(OrderDirection.ASC);

            List<Message> messages = Collections.emptyList();

            if (subscriptionMessageFilter.getStartDate() != null) {
                messageFilter.setMinPublicationDate(subscriptionMessageFilter.getStartDate());
                if (subscription.getLastProcessedMessagePublicationDate() != null
                        && subscription.getLastProcessedMessagePublicationDate().after(
                                subscriptionMessageFilter.getStartDate())) {
                    messageFilter.setMinPublicationDate(subscription
                            .getLastProcessedMessagePublicationDate());
                }
                messages = persistence.getMessages(messageFilter);
            }
            if (messages.size() < subscriptionMessageFilter.getLastXMessages()) {

                messageFilter.setMinPublicationDate(subscription
                        .getLastProcessedMessagePublicationDate());
                messageFilter.setLastMessagesCount(subscriptionMessageFilter.getLastXMessages());
                messages = persistence.getMessages(messageFilter);
            }

            Collections.sort(messages, MessagePublicationDateComperator.INSTANCE);

            for (Message message : messages) {

                AggregatorMessageContext aggregatorMessageContext = new AggregatorMessageContext(
                        aggregatorChain.getPersistence(), message, subscription.getGlobalId());
                aggregatorChain.getAddMessageToSubscriptionChain().process(
                        aggregatorMessageContext);
            }

        }
    }

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

    private ExecutorService messagePushService;
    private boolean messagePushServiceStopped;

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
        List<SourceStatus> persistentSourceStatus = persistence.getSourceStatusList();
        LOGGER.debug("adding {} subscriptions from persistence", persistentSourceStatus.size());

        for (SourceStatus sourceStatus : persistentSourceStatus) {
            // LOGGER.warn("To schedule ...: {}", subscription);
            if (sourceStatus.isBlocked()) {
                continue;
            }
            String sourceType = sourceStatus.getSource().getConnectorType();
            Adapter adapter = adapterManager.getAdapter(sourceType);
            if (adapter == null) {
                LOGGER.warn("no adapter implementation for sourceType {} available", sourceType);
                continue;
            }
            adapter.addSource(sourceStatus);
        }
    }

    /**
     * 
     * @param sourceStatus
     * @param sourceStatusProperties
     * @return true if at least one property has been added
     */
    private boolean addProperties(SourceStatus sourceStatus,
            Collection<Property> sourceStatusProperties) {

        boolean modified = false;
        if (sourceStatusProperties != null) {
            for (Property prop : sourceStatusProperties) {

                Property existing = sourceStatus.getProperty(prop.getPropertyKey());
                if (existing == null
                        || StringUtils.equals(existing.getPropertyValue(), prop.getPropertyValue())) {
                    // add it only if the value is different
                    sourceStatus.addProperty(prop);
                    modified = true;
                }
            }
        }

        return modified;

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
    public boolean continueSubscription(String subscriptionGlobalId)
            throws SubscriptionNotFoundException {

        if (subscriptionGlobalId == null) {
            throw new IllegalArgumentException("subscriptionId cannot be null.");
        }
        Subscription subscription = this.persistence
                .getSubscriptionByGlobalId(subscriptionGlobalId);
        if (subscription == null) {
            LOGGER.warn("no subscription with id {}", subscriptionGlobalId);
            return false;
        }
        if (!subscription.isSuspended()) {
            LOGGER.debug("subscription with id {} already active, no action will be taken.",
                    subscriptionGlobalId);
            return false;
        }

        subscription.setSuspended(false);

        this.persistence.updateSubscription(subscription);

        return true;
    }

    private SourceStatus createAndStartSource(Adapter adapter, Source source,
            Collection<Property> sourceStatusProperties) {

        SourceStatus sourceStatus = new SourceStatus(source);
        this.addProperties(sourceStatus, sourceStatusProperties);
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

    @Override
    public List<SourceStatus> findSourceStatusByProperty(Property property) {
        return this.persistence.findSourceStatusByProperty(property);
    }

    /**
     * Get the adaptor for handling a source
     * 
     * @param source
     *            the source
     * @return the adapter
     * @throws AdapterNotFoundException
     *             in case there is no matching adapter
     */
    private Adapter getAdapter(Source source) throws AdapterNotFoundException {
        Adapter adapter = adapterManager.getAdapter(source.getConnectorType());
        if (adapter == null) {
            LOGGER.warn("no adapter implementation for connector type {} available",
                    source.getConnectorType());
            throw new AdapterNotFoundException("No adapter for connector="
                    + source.getConnectorType(), source);
        }
        return adapter;
    }

    private ExecutorService getAsyncMessagePushService() {
        if (this.messagePushService == null) {
            synchronized (this) {
                if (this.messagePushService == null && !this.messagePushServiceStopped) {
                    this.messagePushService = Executors.newSingleThreadExecutor();
                }
            }
        }
        return this.messagePushService;
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
    public Subscription getSubscription(String subscriptionGlobalId)
            throws SubscriptionNotFoundException {
        return this.persistence.getSubscriptionByGlobalId(subscriptionGlobalId);
    }

    @Override
    public List<Subscription> getSubscriptions(SubscriptionFilter subscriptionFilter) {
        return this.persistence.getSubscriptions(subscriptionFilter);
    }

    @Override
    public List<SubscriptionSourceStatus> getSubscriptionsWithSourceStatus(
            SubscriptionFilter subscriptionFilter) {
        return this.persistence.getSubscriptionsWithSourceStatus(subscriptionFilter);
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
        if (errors > 0 && exception != null) {
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
     * Push existing messages to subscription
     * 
     * @param subscription
     * @param subscriptionMessageFilter
     * @param existingSource
     */
    private void pushMessages(Subscription subscription,
            SubscriptionMessageFilter subscriptionMessageFilter, Source existingSource) {
        ExecutorService messagePusher = getAsyncMessagePushService();
        if (messagePusher != null) {
            messagePusher.execute(new PushMessagesTask(subscription, subscriptionMessageFilter,
                    existingSource.getGlobalId()));
        }
    }

    /**
     * tells the adapterManager to stop
     */
    @Override
    public void stop() {
        stopAsyncMessagePushService();
        adapterManager.stop();
    }

    /**
     * Stop the asynchronous message push service.
     */
    private synchronized void stopAsyncMessagePushService() {
        this.messagePushServiceStopped = true;
        if (this.messagePushService != null) {
            LOGGER.info("Stopping async push service for existing messages");
            this.messagePushService.shutdown();
            try {
                if (!this.messagePushService.awaitTermination(60, TimeUnit.SECONDS)) {
                    this.messagePushService.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!this.messagePushService.awaitTermination(60, TimeUnit.SECONDS)) {
                        LOGGER.error("AsyncMessagePushService did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                this.messagePushService.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
            this.messagePushService = null;
        }
    }

    @Override
    public void subscribe(Subscription subscription) throws AdapterNotFoundException,
            SubscriptionNotFoundException, SubscriptionAlreadyExistsException,
            AccessParameterValidationException {
        this.subscribe(subscription, null, null);
    }

    @Override
    public void subscribe(Subscription subscription,
            SubscriptionMessageFilter subscriptionMessageFilter) throws AdapterNotFoundException,
            SubscriptionAlreadyExistsException, AccessParameterValidationException {
        this.subscribe(subscription, subscriptionMessageFilter, null);
    }

    @Override
    public void subscribe(Subscription subscription,
            SubscriptionMessageFilter subscriptionMessageFilter,
            Collection<Property> sourceStatusProperties) throws AdapterNotFoundException,
            SubscriptionAlreadyExistsException, AccessParameterValidationException {
        LOGGER.debug("Handle subscription " + subscription);
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

        try {
            Subscription existingSubscription = this.persistence
                    .getSubscriptionByGlobalId(subscription.getGlobalId());
            if (existingSubscription != null) {
                LOGGER.debug("Existing subscription found, not subscribing again."
                        + subscription.getGlobalId());
                throw new SubscriptionAlreadyExistsException(subscription.getGlobalId(),
                        subscription);
            }
        } catch (SubscriptionNotFoundException e) {
            // ignore
        }

        Adapter adapter = getAdapter(source);
        adapter.processAccessParametersBeforeSubscribing(source.getAccessParameters());
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
            if (this.addProperties(sourceStatus, sourceStatusProperties)) {
                this.persistence.updateSourceStatus(sourceStatus);
            }

            // push existing messages to the subscription
            if (subscriptionMessageFilter != null
                    && !SubscriptionMessageFilter.NONE.equals(subscriptionMessageFilter)) {
                pushMessages(subscription, subscriptionMessageFilter, existingSource);
            }

        } else {
            // everything is new so create subscription with source and a new source status
            subscription = this.persistence.storeSubscription(subscription);

            // create subscription and source and start working it
            sourceStatus = this.createAndStartSource(adapter, subscription.getSource(),
                    sourceStatusProperties);

        }

    }

    @Override
    public boolean suspendSubscription(String subscriptionId) throws SubscriptionNotFoundException {

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

    @Override
    public void synchronizeSubscriptions(List<Subscription> currentSubscriptions)
            throws AdapterNotFoundException, SubscriptionAlreadyExistsException,
            AccessParameterValidationException {
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
        try {
            // remove old subscriptions
            for (String subscriptionGlobalId : subscriptionsToRemove) {

                unsubscribe(subscriptionGlobalId);
            }
            // update the current subscriptions if neccessary
            for (Subscription subscription : currentSubscriptions) {
                updateOrCreate(subscription);
            }
        } catch (SubscriptionNotFoundException e) {
            // should not occur since only subscriptions are used that exist
            throw new RuntimeException(e);
        }

    }

    private void unblockSource(SourceStatus sourceStatus) {
        LOGGER.info("Unblocking source: {}", sourceStatus);

        sourceStatus.setBlocked(false);
        this.persistence.updateSourceStatus(sourceStatus);

        Adapter adapter = this.adapterManager.getAdapter(sourceStatus.getSource());
        adapter.addSource(sourceStatus);
    }

    @Override
    public void unsubscribe(String subscriptionGlobalId) throws SubscriptionNotFoundException {
        if (subscriptionGlobalId == null) {
            throw new IllegalArgumentException("subscriptionId cannot be null.");
        }
        Subscription subscription = persistence.getSubscriptionByGlobalId(subscriptionGlobalId);
        Source source = subscription.getSource();

        persistence.deleteSubscription(subscriptionGlobalId);

        // TODO this is not thread-safe. Transaction isolation is another thing to consider!
        int numberOfSubscriptionsForSource = persistence
                .getNumberOfSubscriptionsBySourceGlobalId(source.getGlobalId());
        if (numberOfSubscriptionsForSource == 0) {
            LOGGER.info("No more subscriptions found for source: " + source.getGlobalId()
                    + " Will remove it.");
            String connectorType = subscription.getSource().getConnectorType();
            if (connectorType == null) {
                LOGGER.warn("no source type specified for subscription with id {}",
                        subscriptionGlobalId);
                return;
            }
            Adapter adapter = adapterManager.getAdapter(connectorType);
            if (adapter == null) {
                LOGGER.warn("no adapter for source type {}", connectorType);
                return;
            }
            adapter.removeSource(source.getGlobalId());

            this.persistence.deleteSource(source.getGlobalId());
            LOGGER.info("Removed source: " + source.getGlobalId());
        }
    }

    /**
     * updates a {@link Subscription} if necessary
     * 
     * TODO: if it is a update it should somehow be specified if old messages should be pushed to
     * the subscription or not, and the lastprocessedmessage date should be reset or not.
     * 
     * Case 1: URLs is slighlty changed but it is still referring to the same resource / feed (e.g.
     * adding a parameter) Case 2: Compeltly different resource
     * 
     * In case 1 the user probably does not want old already pushed messages to be pushed again In
     * case 2 the user probably wants it all.
     * 
     * @param subscription
     *            subscription
     * @return false if it is an update or true an create
     * @throws AdapterNotFoundException
     * @throws SubscriptionNotFoundException
     * @throws SubscriptionAlreadyExistsException
     * @throws AccessParameterValidationException
     */
    public boolean updateOrCreate(Subscription subscription) throws AdapterNotFoundException,
            SubscriptionNotFoundException, SubscriptionAlreadyExistsException,
            AccessParameterValidationException {

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

                subscription.setSource(persistedSource);
                this.persistence.updateSubscription(subscription);

                // finally check if the source is blocked so a subscribe will unblock it
                if (sourceStatus.isBlocked()) {
                    this.unblockSource(sourceStatus);
                }
                return false;
            }

        }

        if (persistentSubscription != null) {
            // in this case the source of the subscription changed. therefore unsubscribe it, and
            // subcribe it below
            this.unsubscribe(subscription.getGlobalId());
        }

        subscribe(subscription);

        return true;
    }

    @Override
    public void updateSourceAccessParameter(String sourceGlobalId,
            Collection<Property> accessParameters) throws SourceNotFoundException,
            AdapterNotFoundException {
        Source source = this.persistence.getSourceByGlobalId(sourceGlobalId);

        for (Property property : accessParameters) {
            if (property.getPropertyValue() == null) {
                source.removeAccessParameter(property.getPropertyKey());
            } else {
                source.addAccessParameter(property);
            }
        }

        this.persistence.updateSource(source);
        SourceStatus sourceStatus = this.persistence
                .getSourceStatusBySourceGlobalId(sourceGlobalId);

        Adapter adapter = getAdapter(source);
        adapter.addSource(sourceStatus);

    }
}
