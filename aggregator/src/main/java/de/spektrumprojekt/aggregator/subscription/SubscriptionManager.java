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

import java.util.Collection;
import java.util.List;

import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.source.SourceNotFoundException;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.SubscriptionAlreadyExistsException;
import de.spektrumprojekt.datamodel.subscription.SubscriptionFilter;
import de.spektrumprojekt.datamodel.subscription.SubscriptionMessageFilter;
import de.spektrumprojekt.datamodel.subscription.SubscriptionSourceStatus;
import de.spektrumprojekt.exceptions.SubscriptionNotFoundException;

/**
 * <p>
 * A Subscription Manager is responsible for handling {@link Subscription}s. This includes firing up
 * the matching source adapters, and making all processed subscriptions persistent.
 * </p>
 * 
 * @author Philipp Katz
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public interface SubscriptionManager {

    boolean continueSubscription(String subscriptionGlobalId) throws SubscriptionNotFoundException;

    List<SourceStatus> findSourceStatusByProperty(Property property);

    Subscription getSubscription(String subscriptionGlobalId) throws SubscriptionNotFoundException;

    List<Subscription> getSubscriptions(SubscriptionFilter subscriptionFilter);

    List<SubscriptionSourceStatus> getSubscriptionsWithSourceStatus(
            SubscriptionFilter subscriptionFilter);

    /**
     * Stop this manager
     */
    void stop();

    /**
     * <p>
     * Subscribe to the specified subscription.
     * </p>
     * 
     * @param subscription
     *            The subscription to subscribe to.
     * @throws AdapterNotFoundException
     * @throws SubscriptionAlreadyExistsException
     */
    void subscribe(Subscription subscription) throws AdapterNotFoundException,
            SubscriptionNotFoundException, SubscriptionAlreadyExistsException;

    /**
     * <p>
     * Subscribe to the specified subscription.
     * </p>
     * 
     * @param subscription
     *            The subscription to subscribe to.
     * @param subscriptionMessageFilter
     *            Use this filter to also return the messages already fetched. If null
     *            {@link SubscriptionMessageFilter#NONE} will be used.
     * @param sourceStatusProperties
     * @throws AdapterNotFoundException
     * @throws SubscriptionAlreadyExistsException
     */
    void subscribe(Subscription subscription,
            SubscriptionMessageFilter subscriptionMessageFilter)
            throws AdapterNotFoundException, SubscriptionAlreadyExistsException;

    /**
     * <p>
     * Subscribe to the specified subscription.
     * </p>
     * 
     * @param subscription
     *            The subscription to subscribe to.
     * @param subscriptionMessageFilter
     *            Use this filter to also return the messages already fetched. If null
     *            {@link SubscriptionMessageFilter#NONE} will be used.
     * @param sourceStatusProperties
     * @throws AdapterNotFoundException
     * @throws SubscriptionAlreadyExistsException
     */
    void subscribe(Subscription subscription,
            SubscriptionMessageFilter subscriptionMessageFilter,
            Collection<Property> sourceStatusProperties)
            throws AdapterNotFoundException, SubscriptionAlreadyExistsException;

    boolean suspendSubscription(String subscriptionId) throws SubscriptionNotFoundException;

    /**
     * Use exactly the subscriptions as in the provided list. Remove all not in the list.
     * 
     * @param subscriptions
     *            the subscriptions to synchronize
     * @throws AdapterNotFoundException
     * @throws SubscriptionAlreadyExistsException
     */
    void synchronizeSubscriptions(List<Subscription> subscriptions)
            throws AdapterNotFoundException, SubscriptionAlreadyExistsException;

    /**
     * <p>
     * Remove the subscription with the specified subscription ID from this subscription manager.
     * </p>
     * 
     * @param subscriptionGlobalId
     *            The subscription global ID for the subscription which shall be removed.
     * @throws SubscriptionNotFoundException
     */
    void unsubscribe(String subscriptionGlobalId) throws SubscriptionNotFoundException;

    void updateSourceAccessParameter(String sourceGlobalId, Collection<Property> accessParameters)
            throws SourceNotFoundException, AdapterNotFoundException;
}
