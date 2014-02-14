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

package de.spektrumprojekt.i.learner.time;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.time.DateUtils;

import de.spektrumprojekt.commons.time.TimeProviderHolder;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.observation.Interest;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserModelEntryTimeBin;
import de.spektrumprojekt.i.learner.contentbased.TermCountUserModelEntryIntegrationStrategy;

public class TimeBinnedUserModelEntryIntegrationStrategy extends
        TermCountUserModelEntryIntegrationStrategy {

    public static long DAY = DateUtils.MILLIS_PER_DAY;
    public static long WEEK = 7 * DateUtils.MILLIS_PER_DAY;
    public static long MONTH = 4 * WEEK;

    public final static TimeBinnedUserModelEntryIntegrationStrategy MONTH_WEEK = new TimeBinnedUserModelEntryIntegrationStrategy(
            0, TimeBinnedUserModelEntryIntegrationStrategy.MONTH,
            TimeBinnedUserModelEntryIntegrationStrategy.WEEK, false);

    public final static TimeBinnedUserModelEntryIntegrationStrategy MONTH_DAY = new TimeBinnedUserModelEntryIntegrationStrategy(
            0, TimeBinnedUserModelEntryIntegrationStrategy.MONTH,
            TimeBinnedUserModelEntryIntegrationStrategy.DAY, false);

    private final long startTime;

    private final long lengthOfAllBinsInMs;

    private final long lengthOfSingleBinInMs;

    private final boolean calculateLater;

    public TimeBinnedUserModelEntryIntegrationStrategy(long startTime, long lengthOfAllBinsInMs,
            long lengthOfSingleBinInMs) {
        this(startTime, lengthOfAllBinsInMs, lengthOfSingleBinInMs, false);

    }

    /**
     * Better to only use the single bin length and the number of bins
     * 
     * @param startTime
     * @param lengthOfAllBinsInMs
     * @param lengthOfSingleBinInMs
     * @param calculateLater
     */
    public TimeBinnedUserModelEntryIntegrationStrategy(
            long startTime,
            long lengthOfAllBinsInMs,
            long lengthOfSingleBinInMs,
            boolean calculateLater) {
        if (lengthOfSingleBinInMs <= 0) {
            throw new IllegalArgumentException("binPrecisionInMs must be > 0 but is "
                    + lengthOfSingleBinInMs);
        }

        int factor = (int) Math.ceil((double) lengthOfAllBinsInMs / lengthOfSingleBinInMs);

        this.startTime = startTime;
        this.lengthOfAllBinsInMs = factor * lengthOfSingleBinInMs;
        this.lengthOfSingleBinInMs = lengthOfSingleBinInMs;
        this.calculateLater = calculateLater;
    }

    @Override
    public Map<Term, UserModelEntry> cleanUpEntries(Map<Term, UserModelEntry> entries) {
        entries = super.cleanUpEntries(entries);

        Map<Term, UserModelEntry> cleanedEntries = new HashMap<Term, UserModelEntry>();

        long minimumTimeBinSizeStart = this
                .determineFirstTimeBinStart(TimeProviderHolder.DEFAULT.getCurrentTime());
        for (Entry<Term, UserModelEntry> entry : entries.entrySet()) {

            entry.getValue().cleanUpTimeBins(minimumTimeBinSizeStart);

            if (!calculateLater) {
                entry.getValue().consolidateByTimeBins();
            }
            if (entry.getValue().getTimeBinEntries().size() > 0) {
                cleanedEntries.put(entry.getKey(), entry.getValue());

            }
        }
        return cleanedEntries;
    }

    public int determineBinIndex(long time) {
        long first = determineFirstTimeBinStart(time);
        long current = determineCurrentTimeBinStart(time);
        int index = (int) ((current - first) / this.lengthOfSingleBinInMs);
        return index;
    }

    /**
     * Example: <br>
     * lengthOfSingleBinInMs is the time interval for a day<br>
     * startTime is 0 (default)<br>
     * time given is 2011-12-12 4:12pm<br>
     * than the return will be 2011-12-12 0:00<br>
     * 
     * @param time
     * @return the time the current bin started for the given time
     */
    public long determineCurrentTimeBinStart(long time) {
        long index = (time - startTime) / lengthOfSingleBinInMs;
        return index * lengthOfSingleBinInMs;
    }

    /**
     * Example: <br>
     * lengthOfAllBinsInMs is the time interval for a month<br>
     * startTime is 0 (default)<br>
     * time given is 2011-12-12<br>
     * than the return will be 2011-12-01<br>
     * 
     * @param time
     * @return the time the first bin started
     */
    public long determineFirstTimeBinStart(long time) {
        long index = (time - startTime) / lengthOfAllBinsInMs;
        return index * lengthOfAllBinsInMs;
    }

    @Override
    public boolean disintegrate(UserModelEntry entry, Interest interest, ScoredTerm scoredTerm,
            Date observationDate) {
        if (observationDate == null) {
            throw new IllegalArgumentException("observationDate cannot be null.");
        }
        return updateEntry(entry, -1 * interest.getScore(), scoredTerm, observationDate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return toString();
    }

    public long getLengthOfAllBinsInMs() {
        return lengthOfAllBinsInMs;
    }

    public long getLengthOfSingleBinInMs() {
        return lengthOfSingleBinInMs;
    }

    public long getStartTime() {
        return startTime;
    }

    @Override
    public boolean integrate(UserModelEntry entry, Interest interest, ScoredTerm scoredTerm,
            Date observationDate) {
        if (observationDate == null) {
            throw new IllegalArgumentException("observationDate cannot be null.");
        }
        return updateEntry(entry, interest.getScore(), scoredTerm, observationDate);

    }

    public boolean isCalculateLater() {
        return calculateLater;
    }

    public boolean isTimeBinChanged(Date lastTimeBinChecked) {
        long lastStart = determineFirstTimeBinStart(lastTimeBinChecked.getTime());
        long currentStart = determineFirstTimeBinStart(TimeProviderHolder.DEFAULT.getCurrentTime());

        return lastStart < currentStart;
    }

    @Override
    public String toString() {
        return "TimeBinnedUserModelEntryIntegrationStrategy [startTime=" + startTime
                + ", lengthOfAllBinsInMs=" + lengthOfAllBinsInMs + ", lengthOfSingleBinInMs="
                + lengthOfSingleBinInMs + ", calculateLater=" + calculateLater + "]";
    }

    private boolean updateEntry(UserModelEntry entry, float interestScore, ScoredTerm scoredTerm,
            Date observationDate) {
        if (scoredTerm.getWeight() >= getMinScore()) {

            long currentTime = TimeProviderHolder.DEFAULT.getCurrentTime();

            long timeBinPrecisionStart = determineCurrentTimeBinStart(observationDate.getTime());
            long currentTimeBinSizeStart = determineFirstTimeBinStart(currentTime);

            // time is out of the bin
            if (timeBinPrecisionStart >= currentTimeBinSizeStart) {

                UserModelEntryTimeBin timeBin = entry
                        .getUserModelEntryTimeBinByStartTime(timeBinPrecisionStart);
                if (timeBin == null) {
                    timeBin = new UserModelEntryTimeBin(timeBinPrecisionStart);
                    entry.addTimeBinEntry(timeBin);
                }

                if (interestScore >= 0) {
                    timeBin.setScoreCount(timeBin.getScoreCount() + 1);
                } else {
                    timeBin.setScoreCount(timeBin.getScoreCount() - 1);
                }
                timeBin.setScoreSum(timeBin.getScoreSum() + interestScore);

                // todo: this should be called on every time bin change
                entry.cleanUpTimeBins(currentTimeBinSizeStart);
                if (!calculateLater) {
                    entry.consolidateByTimeBins();
                }
                entry.addToTimeBinEntriesHistory(timeBin);

                int size = entry.getTimeBinEntries() == null ? 0 : entry.getTimeBinEntries().size();
                double ceiledMaxBins = Math
                        .ceil(this.lengthOfAllBinsInMs / (double) this.lengthOfSingleBinInMs);
                if (size > ceiledMaxBins) {
                    throw new IllegalArgumentException(
                            "timeBinEntries.size="
                                    + size
                                    + " is greater than ceil( this.lengthOfAllBinsInMs / this.lengthOfSingleBinInMs) = "
                                    + ceiledMaxBins + ".");
                }
            }
        }
        return entry.getTimeBinEntries() == null || entry.getTimeBinEntries().size() == 0;
    }
}
