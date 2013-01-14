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

package de.spektrumprojekt.aggregator.adapter;

import java.util.Collection;

import de.spektrumprojekt.datamodel.subscription.SubscriptionStatus;

/**
 * <p>
 * Defines an adapter's interface. An adapter is responsible for aggregating
 * information from various web sources, e.g. feeds, Twitter, Communote, etc.
 * This interface defines functionality for subscribing to and unsubscribing
 * from such sources.
 * </p>
 * 
 * @author Philipp Katz
 * 
 */
public interface IAdapter {

	/**
	 * <p>
	 * Adds the specified subscription to the adapter.
	 * </p>
	 * 
	 * @param subscription
	 *            The subscription to add.
	 */
	void addSubscription(SubscriptionStatus subscription);

	/**
	 * <p>
	 * Adds the specified subscriptions to the adapter.
	 * </p>
	 * 
	 * @param subscriptions
	 *            The subscriptions to add.
	 */
	void addSubscriptions(Collection<SubscriptionStatus> subscriptions);

	/**
	 * <p>
	 * Return the source type of this specific adapter, e.g. <code>RSS</code>,
	 * <code>Twitter</code>, etc.
	 * </p>
	 * 
	 * @return The source type.
	 */
	String getSourceType();

	/**
	 * <p>
	 * Returns all Subscriptions managed by this adapter
	 * </p>
	 * 
	 * @return subscriptionGlobalIds
	 */
	Collection<String> getSubscriptionGlobalIds();

	/**
	 * <p>
	 * Removes the specified subscriptions from the adapter.
	 * </p>
	 * 
	 * @param subscriptionId
	 */
	void removeSubscription(String subscriptionId);

	/**
	 * <p>
	 * Register an {@link IAdapterListener} for this adapter.
	 * </p>
	 * 
	 * @param listener
	 *            The listener, not <code>null</code>.
	 */
	void setListener(IAdapterListener listener);

	/**
	 * kill started tasks if necessary
	 */
	void stop();

}
