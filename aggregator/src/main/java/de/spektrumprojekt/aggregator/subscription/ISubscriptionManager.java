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

import de.spektrumprojekt.datamodel.subscription.Subscription;

/**
 * <p>
 * A Subscription Manager is responsible for handling {@link Subscription}s. This includes firing up the matching source
 * adapters, and making all processed subscriptions persistent.
 * </p>
 * 
 * @author Philipp Katz
 * 
 */
public interface ISubscriptionManager {

    /**
     * <p>
     * Subscribe to the specified subscription.
     * </p>
     * 
     * @param subscription The subscription to subscribe to.
     */
    void subscribe(Subscription subscription);

    /**
     * <p>
     * Remove the subscription with the specified subscription ID from this subscription manager.
     * </p>
     * 
     * @param subscriptionId The subscription ID for the subscription which shall be removed.
     */
    void unsubscribe(String subscriptionId);

}
