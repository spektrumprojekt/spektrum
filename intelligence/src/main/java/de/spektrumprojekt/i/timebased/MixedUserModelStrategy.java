package de.spektrumprojekt.i.timebased;

import de.spektrumprojekt.i.learner.time.TimeBinnedUserModelEntryIntegrationStrategy;

public class MixedUserModelStrategy extends TimeBinnedUserModelEntryIntegrationStrategy {

    public MixedUserModelStrategy(long startTime, long binSizeInMs, long binPrecisionInMs) {
        super(startTime, binSizeInMs, binPrecisionInMs);
    }
}
