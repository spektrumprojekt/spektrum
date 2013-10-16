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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;

/**
 * <p>
 * Base implementation for adapters which require continuous polling. The polling is done in fixed,
 * specified time intervals, scheduled by a thread pool. Subclasses need to implement
 * {@link #poll(Subscription)} method.
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * @author Philipp Katz
 */
public abstract class BasePollingAdapter extends BaseAdapter {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(BasePollingAdapter.class);

    /** The time interval which is used for polling the sources. */
    private final int pollingInterval;

    /** The thread pool responsible for executing the polling tasks. */
    private final ScheduledThreadPoolExecutor executor;

    /**
     * Keep mapping from {@link Source#getGlobalId()}s to the scheduled tasks.
     */
    private final Map<String, Future<?>> scheduledSources;

    /**
     * <p>
     * Initialize a new polling adapter.
     * </p>
     * 
     * @param messageQueue
     *            The message queue for outgoing messages.
     * @param pollingInterval
     *            The time interval in seconds in which this adapter polls.
     * @param threadPoolSize
     *            The size of the thread pool for this adapter, i.e. the maximum number of
     *            simultaneous polls.
     */
    public BasePollingAdapter(AggregatorChain aggregatorChain,
            AggregatorConfiguration aggregatorConfiguration, int pollingInterval, int threadPoolSize) {
        super(aggregatorChain, aggregatorConfiguration);

        this.pollingInterval = pollingInterval;
        executor = new ScheduledThreadPoolExecutor(threadPoolSize,
                new PollThreadFactory<BasePollingAdapter>(this));
        scheduledSources = Collections
                .synchronizedMap(new HashMap<String, Future<?>>());
    }

    @Override
    public void addSource(final SourceStatus sourceStatus) {
        LOGGER.debug("Adding source status " + sourceStatus);

        Future<?> existingTask = this.scheduledSources.get(sourceStatus.getSource().getGlobalId());
        if (existingTask != null) {
            LOGGER.info("Source status already in progress. Remove it first. " + sourceStatus);
            this.removeSource(sourceStatus.getGlobalId());
        }

        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    List<Message> messages = poll(sourceStatus);
                    addMessages(messages);
                    triggerListener(sourceStatus.getSource(),
                            StatusType.OK);
                } catch (AdapterException e) {
                    LOGGER.warn("encountered AdapterException {} sourceStatus={}",
                            e.toString(), sourceStatus);
                    LOGGER.debug(e.getMessage(), e);
                    // FIXME send error message here?
                    triggerListener(sourceStatus.getSource(),
                            e.getStatusType(), e);
                } catch (Throwable t) {
                    LOGGER.error("encountered exception " + t + " for " + sourceStatus, t);
                }
            }
        };
        ScheduledFuture<?> scheduledTask = executor.scheduleAtFixedRate(task,
                0, pollingInterval, TimeUnit.SECONDS);
        scheduledSources.put(sourceStatus.getSource()
                .getGlobalId(), scheduledTask);
    }

    @Override
    public Collection<String> getSourceGlobalIds() {
        return scheduledSources.keySet();
    }

    public String getStats() {
        StringBuilder builder = new StringBuilder();
        builder.append("active: ").append(executor.getActiveCount())
                .append('\n');
        builder.append("completed: ").append(executor.getCompletedTaskCount())
                .append('\n');
        builder.append("task count: ").append(executor.getTaskCount())
                .append('\n');
        builder.append("queue size: ").append(executor.getQueue().size());
        return builder.toString();
    }

    /**
     * <p>
     * Do the poll. See {@link #BasePollingAdapter(BlockingQueue, int, int)} on how to setup the
     * polling interval and the thread pool size.
     * </p>
     * 
     * @param sourceStatus
     *            The subscription to poll.
     * @return The (new) messages which have been acquired by this adapter.
     * @throws AdapterException
     *             In case polling fails.
     */
    public abstract List<Message> poll(SourceStatus sourceStatus)
            throws AdapterException;

    @Override
    public void removeSource(String sourceGlobalId) {
        Future<?> task = scheduledSources.remove(sourceGlobalId);
        if (task != null) {
            task.cancel(false);
            LOGGER.debug("removed source {}", sourceGlobalId);
        } else {
            LOGGER.error("no source with ID {}", sourceGlobalId);
        }
    }

    @Override
    public void stop() {
        executor.shutdownNow();
    }
}
