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

import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;

/**
 * <p>
 * A listener which can be registered for {@link IAdapter}s.
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * @author Philipp Katz
 */
public interface AdapterListener {

    /**
     * <p>
     * Triggered, when a {@link Subscription} was processed by the {@link IAdapter}.
     * </p>
     * 
     * @param source
     *            The processed subscription.
     * @param statusType
     *            The resulting {@link StatusType}.
     */
    void processed(Source source, StatusType statusType);

    /**
     * <p>
     * Triggered, when a {@link Subscription} was processed by the {@link IAdapter}.
     * </p>
     * 
     * @param source
     *            The processed subscription.
     * @param statusType
     *            The resulting {@link StatusType}.
     * @param exception
     *            exception which occurred
     */
    void processed(Source source, StatusType statusType, Exception exception);
}
