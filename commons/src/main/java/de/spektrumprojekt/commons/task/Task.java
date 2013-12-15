package de.spektrumprojekt.commons.task;

import java.util.Date;

import de.spektrumprojekt.commons.computer.Computer;
import de.spektrumprojekt.commons.time.TimeProviderHolder;

/**
 * Keeps information about {@link Computer} to run periodically
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class Task {
    private final Computer computer;

    private Date nextDate;

    private final long intervall;
    private final long delayOnFirstStart;
    private final boolean runExactIntervall;

    /**
     * 
     * @param computer
     * @param intervall
     * @param delayOnFirstStart
     * @param runExactIntervall
     *            if true the next computation date will be determined by the currents execution
     *            date. Otherwise the intervall will be used based on the current execution date.
     *            So, if this value is true, and the execution itself took 1 hour and the execution
     *            intervall is 1 day it will run again in 23 hours and if false in 24 hours.
     */
    public Task(Computer computer, long intervall, long delayOnFirstStart, boolean runExactIntervall) {
        if (computer == null) {
            throw new IllegalArgumentException("computer cannot be null.");
        }
        if (intervall <= 0) {
            throw new IllegalArgumentException("intervall must be greater 0. intervall="
                    + intervall);
        }
        this.computer = computer;
        this.intervall = intervall;
        this.delayOnFirstStart = delayOnFirstStart;
        this.runExactIntervall = runExactIntervall;

        this.incrementNextDate();
    }

    public Computer getComputer() {
        return computer;
    }

    public long getDelayOnFirstStart() {
        return delayOnFirstStart;
    }

    public long getIntervall() {
        return intervall;
    }

    public Date getNextDate() {
        return nextDate;
    }

    public void incrementNextDate() {
        long next = 0;
        if (nextDate == null) {
            next = TimeProviderHolder.DEFAULT.getCurrentTime();
            next += delayOnFirstStart;
        } else {
            next = runExactIntervall ? nextDate.getTime() : TimeProviderHolder.DEFAULT
                    .getCurrentTime();
            next += intervall;
        }
        this.nextDate = new Date(next);
    }

    @Override
    public String toString() {
        return "Task [computer=" + computer + ", nextDate=" + nextDate + ", intervall=" + intervall
                + ", delayOnFirstStart=" + delayOnFirstStart + ", runExactIntervall="
                + runExactIntervall + "]";
    }
}