package de.spektrumprojekt.i.timebased;

import de.spektrumprojekt.i.learner.time.TimeBinnedUserModelEntryIntegrationStrategy;

public class NutritionAndEnergyIntegrationStrategy extends
        TimeBinnedUserModelEntryIntegrationStrategy {

    public NutritionAndEnergyIntegrationStrategy(long startTime, long binSizeInMs,
            long binPrecisionInMs) {
        super(startTime, binSizeInMs, binPrecisionInMs, true);
    }
}
