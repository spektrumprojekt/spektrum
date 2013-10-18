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
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;

import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.chain.AggregatorMessageContext;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePublicationDateComperator;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;

/**
 * <p>
 * Base implementation for an aggregator adapter. Adapter implementations allow to consume different
 * source formats, like web feeds, and transform them into the {@link Message} data format.
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * @author Philipp Katz
 */
public abstract class BaseAdapter implements Adapter {

    private AdapterListener listener;

    private final AggregatorChain aggregatorChain;

    private final AggregatorConfiguration aggregatorConfiguration;

    /**
     * <p>
     * Initialize a new Adapter with the specified message queue, where all the outgoing messages
     * are put.
     * </p>
     * 
     * @param aggregatorConfiguration
     * 
     * @param messageQueue
     *            The outgoing message queue for this adapter.
     */
    public BaseAdapter(AggregatorChain aggregatorChain,
            AggregatorConfiguration aggregatorConfiguration) {
        if (aggregatorConfiguration == null) {
            throw new IllegalArgumentException("aggregatorConfiguration cannot be null.");
        }
        if (aggregatorChain == null) {
            throw new IllegalArgumentException("aggregatorChain cannot be null.");
        }
        this.aggregatorConfiguration = aggregatorConfiguration;
        this.aggregatorChain = aggregatorChain;

    }

    /**
     * <p>
     * Allows subclasses to put acquired messages in the queue.
     * </p>
     * 
     * @param message
     *            The message to put into the queue.
     */
    protected final void addMessage(Message message) {
        AggregatorMessageContext aggregatorMessageContext = new AggregatorMessageContext(
                this.aggregatorChain.getPersistence(), message);
        this.aggregatorChain.getNewMessageChain().process(aggregatorMessageContext);
    }

    /**
     * <p>
     * Allows subclasses to put acquired messages in the queue.
     * </p>
     * 
     * @param messages
     *            The messages to put into the queue.
     */
    protected void addMessages(List<Message> messages) {
        Collections.sort(messages, MessagePublicationDateComperator.INSTANCE);

        for (Message message : messages) {
            addMessage(message);
        }
    }

    @Override
    public void addSources(Collection<SourceStatus> subscriptions) {
        for (SourceStatus subscription : subscriptions) {
            addSource(subscription);
        }
    }

    public AggregatorChain getAggregatorChain() {
        return aggregatorChain;
    }

    public AggregatorConfiguration getAggregatorConfiguration() {
        return this.aggregatorConfiguration;
    }

    @Override
    public final void setListener(AdapterListener listener) {
        Validate.notNull(listener, "listener must not be null");
        this.listener = listener;
    }

    protected final void triggerListener(Source source, StatusType statusType) {
        triggerListener(source, statusType, null);
    }

    protected final void triggerListener(Source source, StatusType statusType, Exception exception) {
        if (listener != null) {
            listener.processed(source, statusType, exception);
        }
    }

}
