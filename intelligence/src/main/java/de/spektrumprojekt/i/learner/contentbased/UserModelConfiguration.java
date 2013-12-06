package de.spektrumprojekt.i.learner.contentbased;

import org.apache.commons.lang.time.DateUtils;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

public class UserModelConfiguration implements ConfigurationDescriptable, Cloneable {

    public static long DAY = DateUtils.MILLIS_PER_DAY;
    public static long WEEK = 7 * DateUtils.MILLIS_PER_DAY;
    public static long MONTH = 4 * WEEK;

    public static final UserModelConfiguration MONTH_DAY = new UserModelConfiguration(
            UserModelEntryIntegrationStrategyType.TIMEBINNED, 0, WEEK, MONTH, false);

    public static final UserModelConfiguration MONTH_WEEK = new UserModelConfiguration(
            UserModelEntryIntegrationStrategyType.TIMEBINNED, 0, DAY, MONTH, false);

    public static UserModelConfiguration getShortTermModelConfiguration(long startTime,
            long precision, long binSize) {
        return new UserModelConfiguration(UserModelEntryIntegrationStrategyType.TIMEBINNED,
                startTime,
                precision, binSize, true);
    }

    public static UserModelConfiguration getTimeBinnedModelConfiguration(long startTime,
            long precision, long binSize) {
        return new UserModelConfiguration(UserModelEntryIntegrationStrategyType.TIMEBINNED,
                startTime,
                precision, binSize, false);
    }

    public static UserModelConfiguration getUserModelConfigurationWithIncrementalLearningStrategy() {
        return new UserModelConfiguration(UserModelEntryIntegrationStrategyType.INCREMENTAL);
    }

    public static UserModelConfiguration getUserModelConfigurationWithTermCountLearningStrategy() {
        return new UserModelConfiguration(UserModelEntryIntegrationStrategyType.TERM_COUNT);
    }

    private UserModelEntryIntegrationStrategyType userModelEntryIntegrationStrategyType;

    private long startTime;
    private long precision;
    private long binSize;
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
            long precision,
            long binSize,
            boolean calculateLater) {

        this.userModelEntryIntegrationStrategyType = userModelEntryIntegrationStrategyType;
        this.startTime = startTime;
        this.precision = precision;
        this.binSize = binSize;
        this.calculateLater = calculateLater;
    }

    public long getBinSize() {
        return binSize;
    }

    @Override
    public String getConfigurationDescription() {
        return "UserModelConfiguration [userModelEntryIntegrationStrategyType="
                + userModelEntryIntegrationStrategyType + ", startTime=" + startTime
                + ", precision="
                + precision + ", binSize=" + binSize + ", calculateLater=" + calculateLater + "]";
    }

    public float getIncrementalLearningFactorAlpha() {
        return incrementalLearningFactorAlpha;
    }

    public float getIncrementalLThresholdOfNeutral() {
        return incrementalLThresholdOfNeutral;
    }

    public long getPrecision() {
        return precision;
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

    public void setBinSize(long binSize) {
        this.binSize = binSize;
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

    public void setPrecision(long precision) {
        this.precision = precision;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setUserModelEntryIntegrationStrategyType(
            UserModelEntryIntegrationStrategyType userModelEntryIntegrationStrategyType) {
        this.userModelEntryIntegrationStrategyType = userModelEntryIntegrationStrategyType;
    }
}