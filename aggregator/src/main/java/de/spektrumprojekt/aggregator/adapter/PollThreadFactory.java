package de.spektrumprojekt.aggregator.adapter;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This Factory creates the Threads which poll. The Name is generated from the Adapter and a running
 * number.
 */
class PollThreadFactory<T extends BasePollingAdapter> implements
        ThreadFactory {

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    PollThreadFactory(T executingClass) {
        SecurityManager s = System.getSecurityManager();
        group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = executingClass.getClass().getSimpleName() + " - Thread - ";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}