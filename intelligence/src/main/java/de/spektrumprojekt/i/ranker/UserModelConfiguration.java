package de.spektrumprojekt.i.ranker;

import org.apache.commons.lang.time.DateUtils;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

public class UserModelConfiguration implements ConfigurationDescriptable, Cloneable {

    public enum UserModelEntryIntegrationStrategy {
        PLAIN, TIMEBINNED;
    }

    public static long DAY = DateUtils.MILLIS_PER_DAY;
    public static long WEEK = 7 * DateUtils.MILLIS_PER_DAY;
    public static long MONTH = 4 * WEEK;

    public static final UserModelConfiguration MONTH_DAY = new UserModelConfiguration(
            UserModelEntryIntegrationStrategy.TIMEBINNED, 0, WEEK, MONTH, false);

    public static final UserModelConfiguration MONTH_WEEK = new UserModelConfiguration(
            UserModelEntryIntegrationStrategy.TIMEBINNED, 0, DAY, MONTH, false);

    public static UserModelConfiguration getPlainModelConfiguration() {
        return new UserModelConfiguration(UserModelEntryIntegrationStrategy.PLAIN);
    }

    public static UserModelConfiguration getShortTermModelConfiguration(long startTime,
            long precision, long binSize) {
        return new UserModelConfiguration(UserModelEntryIntegrationStrategy.TIMEBINNED, startTime,
                precision, binSize, true);
    }

    public static UserModelConfiguration getTimeBinnedModelConfiguration(long startTime,
            long precision, long binSize) {
        return new UserModelConfiguration(UserModelEntryIntegrationStrategy.TIMEBINNED, startTime,
                precision, binSize, false);
    }

    private UserModelEntryIntegrationStrategy userModelEntryIntegrationStrategy;

    private long startTime;

    private long precision;

    private long binSize;

    private boolean calculateLater;

    public UserModelConfiguration(
            UserModelEntryIntegrationStrategy userModelEntryIntegrationStrategy) {
        super();
        this.userModelEntryIntegrationStrategy = userModelEntryIntegrationStrategy;
    }

    public UserModelConfiguration(
            UserModelEntryIntegrationStrategy userModelEntryIntegrationStrategy, long startTime,
            long precision, long binSize, boolean calculateLater) {
        super();
        this.userModelEntryIntegrationStrategy = userModelEntryIntegrationStrategy;
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
        // TODO Auto-generated method stub
        return null;
    }

    public long getPrecision() {
        return precision;
    }

    public long getStartTime() {
        return startTime;
    }

    public UserModelEntryIntegrationStrategy getUserModelEntryIntegrationStrategy() {
        return userModelEntryIntegrationStrategy;
    }

    public boolean isCalculateLater() {
        return calculateLater;
    }

    public void setBinSize(long binSize) {
        this.binSize = binSize;
    }

    public void setCalculateLater(boolean calculateLater) {
        this.calculateLater = calculateLater;
    }

    public void setPrecision(long precision) {
        this.precision = precision;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setUserModelEntryIntegrationStrategy(
            UserModelEntryIntegrationStrategy userModelEntryIntegrationStrategy) {
        this.userModelEntryIntegrationStrategy = userModelEntryIntegrationStrategy;
    }
}
