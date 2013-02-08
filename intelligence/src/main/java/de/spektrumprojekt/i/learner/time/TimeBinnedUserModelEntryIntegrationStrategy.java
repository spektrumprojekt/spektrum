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
import java.util.HashSet;

import de.spektrumprojekt.commons.time.TimeProviderHolder;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.observation.Interest;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserModelEntryTimeBin;
import de.spektrumprojekt.i.learner.UserModelEntryIntegrationPlainStrategy;

public class TimeBinnedUserModelEntryIntegrationStrategy extends
        UserModelEntryIntegrationPlainStrategy {

    public static long DAY = 24 * 60 * 60 * 1000;
    public static long WEEK = 7 * DAY;
    public static long MONTH = 4 * WEEK;

    public final static TimeBinnedUserModelEntryIntegrationStrategy MONTH_WEEK =
            new TimeBinnedUserModelEntryIntegrationStrategy(
                    0,
                    TimeBinnedUserModelEntryIntegrationStrategy.MONTH,
                    TimeBinnedUserModelEntryIntegrationStrategy.WEEK);

    private long startTime;
    private long binSizeInMs;

    private long binPrecisionInMs;

    public TimeBinnedUserModelEntryIntegrationStrategy(long startTime, long binSizeInMs,
            long binPrecisionInMs) {
        this.startTime = startTime;
        this.binSizeInMs = binSizeInMs;
        this.binPrecisionInMs = binPrecisionInMs;
    }

    private long determineTimeBinPrecisionStart(long time) {
        long index = (time - startTime) / binPrecisionInMs;
        return index * binPrecisionInMs;
    }

    private long determineTimeBinSizeStart(long time) {
        long index = (time - startTime) / binSizeInMs;
        return index * binSizeInMs;
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
        return getClass().getSimpleName() + " binSizeInMs=" + binSizeInMs + " binPrecisionInMs="
                + binPrecisionInMs + " startTime=" + startTime;
    }

    @Override
    public boolean integrate(UserModelEntry entry, Interest interest, ScoredTerm scoredTerm,
            Date observationDate) {
        if (observationDate == null) {
            throw new IllegalArgumentException("observationDate cannot be null.");
        }
        return updateEntry(entry, interest.getScore(), scoredTerm, observationDate);

    }

    private boolean updateEntry(UserModelEntry entry, float interestScore, ScoredTerm scoredTerm,
            Date observationDate) {
        if (scoredTerm.getWeight() >= getMinScore()) {

            long currentTime = TimeProviderHolder.DEFAULT.getCurrentTime();

            long timeBinPrecisionStart = determineTimeBinPrecisionStart(observationDate.getTime());
            long currentTimeBinSizeStart = determineTimeBinSizeStart(currentTime);

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

                for (UserModelEntryTimeBin entryTimeBin : new HashSet<UserModelEntryTimeBin>(
                        entry.getTimeBinEntries())) {
                    if (entryTimeBin.getTimeBinStart() < currentTimeBinSizeStart) {
                        entry.getTimeBinEntries().remove(entryTimeBin);
                    }
                }

                entry.consolidateByTimeBins();

                int size = entry.getTimeBinEntries() == null ? 0 : entry.getTimeBinEntries().size();
                assert size <= this.binSizeInMs / this.binPrecisionInMs : "timeBinEntries.size="
                        + size + " is greater than this.binSizeInMs / this.binPrecisionInMs = "
                        + this.binSizeInMs / this.binPrecisionInMs + ".";
            }
        }
        return entry.getTimeBinEntries() == null || entry.getTimeBinEntries().size() == 0;
    }
}
