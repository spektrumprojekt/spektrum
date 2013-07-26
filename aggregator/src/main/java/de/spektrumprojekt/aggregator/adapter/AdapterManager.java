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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import de.spektrumprojekt.aggregator.adapter.ping.PingAdapter;
import de.spektrumprojekt.aggregator.adapter.rss.FeedAdapter;
import de.spektrumprojekt.aggregator.adapter.twitter.TwitterAdapter;
import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;

/**
 * <p>
 * Manages all available adapters for the Aggregator. This might be extended in the future, so that
 * new {@link IAdapter} implementations can be loaded and integrated dynamically.
 * </p>
 * 
 * @author Philipp Katz
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
public final class AdapterManager {

    /** Stores all available adapters, indexed by their source types. */
    private final Map<String, IAdapter> adapters;

    /** The listener which can be hooked into the adapter instances. */
    private final IAdapterListener adapterListener;

    /**
     * <p>
     * Instantiate a new {@link AdapterManager}.
     * </p>
     * 
     * @param communicator
     *            The message queue which is injected into the instantiated {@link IAdapter}
     *            implementations.
     */
    public AdapterManager(AggregatorChain aggregatorChain,
            AggregatorConfiguration aggregatorConfiguration,
            IAdapterListener adapterListener) {

        Validate.notNull(aggregatorChain, "aggregatorChain must not be null");
        Validate.notNull(aggregatorConfiguration, "aggregatorConfiguration must not be null");

        this.adapterListener = adapterListener;
        this.adapters = new HashMap<String, IAdapter>();
        addAdapter(new FeedAdapter(aggregatorChain, aggregatorConfiguration));

        if (aggregatorConfiguration.getTwitterConsumerKey() != null
                && aggregatorConfiguration.getTwitterConsumerSecret() != null) {
            addAdapter(new TwitterAdapter(aggregatorChain, aggregatorConfiguration));
        }
        addAdapter(new PingAdapter(aggregatorChain, aggregatorConfiguration));
    }

    /**
     * <p>
     * Adds an adapter.
     * </p>
     * 
     * @param adapter
     *            The adapter to add, not <code>null</code>.
     */
    public void addAdapter(IAdapter adapter) {
        Validate.notNull(adapter, "adapter must not be null");
        adapters.put(adapter.getSourceType(), adapter);
        if (adapterListener != null) {
            adapter.setListener(adapterListener);
        }
    }

    /**
     * <p>
     * Retrieves the {@link IAdapter} for the specified source type.
     * </p>
     * 
     * @param sourceType
     *            The source type for which to retrieve the adapter.
     * @return The adapter implementation for the specified source type, or <code>null</code> if no
     *         such adapter is available.
     */
    public IAdapter getAdapter(String sourceType) {
        return adapters.get(sourceType);
    }

    public Collection<IAdapter> getAllAdapters() {
        return adapters.values();
    }

    /**
     * calls the adapters stop() methods
     */
    public void stop() {
        for (IAdapter adapter : adapters.values()) {
            adapter.stop();
        }
    }

}
