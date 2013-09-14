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

import de.spektrumprojekt.datamodel.source.SourceStatus;

/**
 * <p>
 * Defines an adapter's interface. An adapter is responsible for aggregating information from
 * various web sources, e.g. feeds, Twitter, Communote, etc. This interface defines functionality
 * for subscribing to and unsubscribing from such sources.
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * @author Philipp Katz
 * 
 */
public interface Adapter {

    /**
     * <p>
     * Adds the specified source to the adapter.
     * </p>
     * 
     * @param source
     *            The subscription to add.
     */
    void addSource(SourceStatus source);

    /**
     * <p>
     * Adds the specified sources to the adapter.
     * </p>
     * 
     * @param source
     *            The sources to add.
     */
    void addSources(Collection<SourceStatus> source);

    /**
     * <p>
     * Returns all source global ids managed by this adapter
     * </p>
     * 
     * @return source global ids (NOT the global ids of the {@link SourceStatus}
     */
    Collection<String> getSourceGlobalIds();

    /**
     * <p>
     * Return the source type of this specific adapter, e.g. <code>RSS</code>, <code>Twitter</code>,
     * etc.
     * </p>
     * 
     * @return The source type.
     */
    String getSourceType();

    /**
     * <p>
     * Removes the specified subscriptions from the adapter.
     * </p>
     * 
     * @param sourceGlobalId
     *            (NOT the global id of the {@link SourceStatus}
     */
    void removeSource(String sourceGlobalId);

    /**
     * <p>
     * Register an {@link AdapterListener} for this adapter.
     * </p>
     * 
     * @param listener
     *            The listener, not <code>null</code>.
     */
    void setListener(AdapterListener listener);

    /**
     * kill started tasks if necessary
     */
    void stop();

}
