package de.spektrumprojekt.i.learner.contentbased;

import org.apache.commons.lang.time.DateUtils;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

public class UserModelConfiguration implements ConfigurationDescriptable, Cloneable {

    public static long DAY = DateUtils.MILLIS_PER_DAY;
    public static long WEEK = 7 * DateUtils.MILLIS_PER_DAY;
    public static long MONTH = 4 * WEEK;

    public static final UserModelConfiguration MONTH_DAY = new UserModelConfiguration(
            UserModelEntryIntegrationStrategyType.TIMEBINNED, 0, DAY, 30, false);

    public static final UserModelConfiguration MONTH_WEEK = new UserModelConfiguration(
            UserModelEntryIntegrationStrategyType.TIMEBINNED, 0, WEEK, 30, false);

    private static int getNumberOfBins(long lengthOfSingleBinInMs, long lengthOfAllBinsInMs) {
        if (lengthOfSingleBinInMs <= 0) {
            throw new IllegalArgumentException(
                    "lengthOfSingleBinInMs must be > 0.");
        }
        if (lengthOfSingleBinInMs > lengthOfAllBinsInMs) {
            throw new IllegalArgumentException(
                    "lengthOfSingleBinInMs must be > lengthOfAllBinsInMs. lengthOfSingleBinInMs="
                            + lengthOfSingleBinInMs + "lengthOfAllBinsInMs=" + lengthOfAllBinsInMs);
        }
        return (int) Math.ceil(lengthOfAllBinsInMs / lengthOfSingleBinInMs);
    }

    public static UserModelConfiguration getShortTermModelConfiguration(
            long startTime,
            long lengthOfSingleBinInMs,
            int numberOfBins) {
        return new UserModelConfiguration(UserModelEntryIntegrationStrategyType.TIMEBINNED,
                startTime, lengthOfSingleBinInMs, numberOfBins, true);
    }

    @Deprecated
    public static UserModelConfiguration getShortTermModelConfiguration(
            long startTime,
            long lengthOfAllBinsInMs,
            long lengthOfSingleBinInMs) {
        int numberOfBins = getNumberOfBins(lengthOfSingleBinInMs, lengthOfAllBinsInMs);
        return new UserModelConfiguration(UserModelEntryIntegrationStrategyType.TIMEBINNED,
                startTime, lengthOfSingleBinInMs, numberOfBins, true);
    }

    public static UserModelConfiguration getTimeBinnedModelConfiguration(long startTime,
            long lengthOfSingleBinInMs, int numberOfBins) {
        return new UserModelConfiguration(
                UserModelEntryIntegrationStrategyType.TIMEBINNED,
                startTime,
                lengthOfSingleBinInMs,
                numberOfBins,
                false);
    }

    @Deprecated
    public static UserModelConfiguration getTimeBinnedModelConfiguration(long startTime,
            long lengthOfAllBinsInMs, long lengthOfSingleBinInMs) {
        int numberOfBins = getNumberOfBins(lengthOfSingleBinInMs, lengthOfAllBinsInMs);
        return new UserModelConfiguration(
                UserModelEntryIntegrationStrategyType.TIMEBINNED,
                startTime,
                lengthOfSingleBinInMs,
                numberOfBins,
                false);
    }

    public static UserModelConfiguration getUserModelConfigurationWithIncrementalLearningStrategy() {
        return new UserModelConfiguration(UserModelEntryIntegrationStrategyType.INCREMENTAL);
    }

    public static UserModelConfiguration getUserModelConfigurationWithTermCountLearningStrategy() {
        return new UserModelConfiguration(UserModelEntryIntegrationStrategyType.TERM_COUNT);
    }

    private UserModelEntryIntegrationStrategyType userModelEntryIntegrationStrategyType;
    private long startTime;
    private long lengthOfSingleBinInMs;
    private int numberOfBins;

    private boolean calculateLater;
    private float incrementalLearningFactorAlpha = 0.25f;
    private float incrementalLThresholdOfNeutral = 0.5f;

    private boolean incrementalLUseCurrentUserModelEntryValueAsThreshold = true;

    public UserModelConfiguration(
            UserModelEntryIntegrationStrategyType userModelEntryIntegrationStrategyType) {
        this.userModelEntryIntegrationStrategyType = userModelEntryIntegrationStrategyType;
    }

    public UserModelConfiguration(
            UserModelEntryIntegrationStrategyType userModelEntryIntegrationStrategyType,
            long startTime,
            long lengthOfSingleBinInMs,
            int numberOfBins,
            boolean calculateLater) {

        this.userModelEntryIntegrationStrategyType = userModelEntryIntegrationStrategyType;
        this.startTime = startTime;
        this.lengthOfSingleBinInMs = lengthOfSingleBinInMs;
        this.numberOfBins = numberOfBins;
        this.calculateLater = calculateLater;
    }

    @Override
    public String getConfigurationDescription() {
        return toString();
    }

    public float getIncrementalLearningFactorAlpha() {
        return incrementalLearningFactorAlpha;
    }

    public float getIncrementalLThresholdOfNeutral() {
        return incrementalLThresholdOfNeutral;
    }

    public long getLengthOfAllBinsInMs() {
        return this.lengthOfSingleBinInMs * this.numberOfBins;
    }

    public long getLengthOfSingleBinInMs() {
        return lengthOfSingleBinInMs;
    }

    public int getNumberOfBins() {
        return numberOfBins;
    }

    public long getStartTime() {
        return startTime;
    }

    public UserModelEntryIntegrationStrategyType getUserModelEntryIntegrationStrategyType() {
        return userModelEntryIntegrationStrategyType;
    }

    public boolean isCalculateLater() {
        return calculateLater;
    }

    public boolean isIncrementalLUseCurrentUserModelEntryValueAsThreshold() {
        return incrementalLUseCurrentUserModelEntryValueAsThreshold;
    }

    public void setCalculateLater(boolean calculateLater) {
        this.calculateLater = calculateLater;
    }

    public void setIncrementalLearningFactorAlpha(float incrementalLearningFactorAlpha) {
        this.incrementalLearningFactorAlpha = incrementalLearningFactorAlpha;
    }

    public void setIncrementalLThresholdOfNeutral(float incrementalLThresholdOfNeutral) {
        this.incrementalLThresholdOfNeutral = incrementalLThresholdOfNeutral;
    }

    public void setIncrementalLUseCurrentUserModelEntryValueAsThreshold(
            boolean incrementalLUseCurrentUserModelEntryValueAsThreshold) {
        this.incrementalLUseCurrentUserModelEntryValueAsThreshold = incrementalLUseCurrentUserModelEntryValueAsThreshold;
    }

    public void setLengthOfSingleBinInMs(long lengthOfSingleBinInMs) {
        this.lengthOfSingleBinInMs = lengthOfSingleBinInMs;

    }

    public void setNumberOfBins(int numberOfBins) {
        this.numberOfBins = numberOfBins;
    }

    /**
     * 
     * @param lengthOfAllBinsInMs
     */
    private void setNumberOfBinsByLengthOfAllBins(long lengthOfAllBinsInMs) {

        this.numberOfBins = getNumberOfBins(this.lengthOfSingleBinInMs, lengthOfAllBinsInMs);

    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setUserModelEntryIntegrationStrategyType(
            UserModelEntryIntegrationStrategyType userModelEntryIntegrationStrategyType) {
        this.userModelEntryIntegrationStrategyType = userModelEntryIntegrationStrategyType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserModelConfiguration [userModelEntryIntegrationStrategyType=");
        builder.append(userModelEntryIntegrationStrategyType);
        builder.append(", startTime=");
        builder.append(startTime);
        builder.append(", lengthOfSingleBinInMs=");
        builder.append(lengthOfSingleBinInMs);
        builder.append(", numberOfBins=");
        builder.append(numberOfBins);
        builder.append(", calculateLater=");
        builder.append(calculateLater);
        builder.append(", incrementalLearningFactorAlpha=");
        builder.append(incrementalLearningFactorAlpha);
        builder.append(", incrementalLThresholdOfNeutral=");
        builder.append(incrementalLThresholdOfNeutral);
        builder.append(", incrementalLUseCurrentUserModelEntryValueAsThreshold=");
        builder.append(incrementalLUseCurrentUserModelEntryValueAsThreshold);
        builder.append("]");
        return builder.toString();
    }
}
