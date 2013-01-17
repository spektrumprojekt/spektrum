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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.SubscriptionStatus;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.persistence.Persistence;

/**
 * <p>
 * Base implementation for adapters which require continuous polling. The polling is done in fixed,
 * specified time intervals, scheduled by a thread pool. Subclasses need to implement
 * {@link #poll(Subscription)} method.
 * </p>
 * 
 * @author Philipp Katz
 */
public abstract class BasePollingAdapter extends BaseAdapter {

    /**
     * This Factory creates the Threads which poll. The Name is generated from the Adapter and a
     * running number.
     */
    static class PollThreadFactory<T extends BasePollingAdapter> implements
            ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        PollThreadFactory(T executingClass) {
            SecurityManager s = System.getSecurityManager();
            group = s != null ? s.getThreadGroup() : Thread.currentThread()
                    .getThreadGroup();
            namePrefix = executingClass.getClass().getSimpleName()
                    + " - Thread - ";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix
                    + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(BasePollingAdapter.class);

    /** The time interval which is used for polling the subscriptions. */
    private final int pollingInterval;

    /** The thread pool responsible for executing the polling tasks. */
    private final ScheduledThreadPoolExecutor executor;

    /**
     * Keep mapping from {@link Subscription#getSubscriptionID()}s to the scheduled tasks.
     */
    private final Map<String, Future<?>> scheduledSubscriptions;

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
    public BasePollingAdapter(Communicator communicator, Persistence persistence,
            AggregatorConfiguration aggregatorConfiguration,
            int pollingInterval, int threadPoolSize) {
        super(communicator, persistence, aggregatorConfiguration);
        this.pollingInterval = pollingInterval;
        executor = new ScheduledThreadPoolExecutor(threadPoolSize,
                new PollThreadFactory<BasePollingAdapter>(this));
        scheduledSubscriptions = Collections
                .synchronizedMap(new HashMap<String, Future<?>>());
    }

    @Override
    public void addSubscription(final SubscriptionStatus subscriptionStatus) {
        LOGGER.debug("adding subscription " + subscriptionStatus);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    List<Message> messages = poll(subscriptionStatus);
                    addMessages(messages);
                    triggerListener(subscriptionStatus.getSubscription(),
                            StatusType.OK);
                } catch (AdapterException e) {
                    LOGGER.warn("encountered AdapterException {} subscription={}",
                            e.toString(), subscriptionStatus);
                    LOGGER.debug(e.getMessage(), e);
                    // FIXME send error message here?
                    triggerListener(subscriptionStatus.getSubscription(),
                            e.getStatusType(), e);
                } catch (Throwable t) {
                    LOGGER.error("encountered exception " + t + " for " + subscriptionStatus, t);
                }
            }
        };
        ScheduledFuture<?> scheduledTask = executor.scheduleAtFixedRate(task,
                0, pollingInterval, TimeUnit.SECONDS);
        scheduledSubscriptions.put(subscriptionStatus.getSubscription()
                .getGlobalId(), scheduledTask);
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

    @Override
    public Collection<String> getSubscriptionGlobalIds() {
        return scheduledSubscriptions.keySet();
    }

    /**
     * <p>
     * Do the poll. See {@link #BasePollingAdapter(BlockingQueue, int, int)} on how to setup the
     * polling interval and the thread pool size.
     * </p>
     * 
     * @param subscription
     *            The subscription to poll.
     * @return The (new) messages which have been acquired by this adapter.
     * @throws AdapterException
     *             In case polling fails.
     */
    public abstract List<Message> poll(SubscriptionStatus subscription)
            throws AdapterException;

    @Override
    public void removeSubscription(String subscriptionId) {
        Future<?> task = scheduledSubscriptions.remove(subscriptionId);
        if (task != null) {
            task.cancel(false);
            LOGGER.debug("removed subscription {}", subscriptionId);
        } else {
            LOGGER.error("no subscription with ID {}", subscriptionId);
        }
    }

    @Override
    public void stop() {
        executor.shutdownNow();
    }
}
