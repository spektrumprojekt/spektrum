package de.spektrumprojekt.i.timebased.config;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

public class LongTermMemoryConfiguration implements ConfigurationDescriptable {

    private int periodicInterestOccuranceCount;

    private int periodicInterestDistanceInBins;

    private float periodicInterestScoreThreshold;

    private float permanentInterestScoreThreshold;

    private int permanentInterestOccurenceMinLengthInBins;

    private float permanentInterestBinsFilledPercentage;

    public LongTermMemoryConfiguration() {
        super();
    }

    public LongTermMemoryConfiguration(int periodicInterestOccuranceCount,
            int periodicInterestDistanceInBins, float periodicInterestScoreThreshold,
            float permanentInterestScoreThreshold, int permanentInterestOccurenceMinLengthInBins) {
        super();
        this.periodicInterestOccuranceCount = periodicInterestOccuranceCount;
        this.periodicInterestDistanceInBins = periodicInterestDistanceInBins;
        this.periodicInterestScoreThreshold = periodicInterestScoreThreshold;
        this.permanentInterestScoreThreshold = permanentInterestScoreThreshold;
        this.permanentInterestOccurenceMinLengthInBins = permanentInterestOccurenceMinLengthInBins;
    }

    @Override
    public String getConfigurationDescription() {
        return "LongTermMemoryConfiguration [periodicInterestOccuranceCount="
                + periodicInterestOccuranceCount + ", periodicInterestDistanceInBins="
                + periodicInterestDistanceInBins + ", periodicInterestScoreThreshold="
                + periodicInterestScoreThreshold + ", permanentInterestScoreThreshold="
                + permanentInterestScoreThreshold + ", permanentInterestOccurenceMinLengthInBins="
                + permanentInterestOccurenceMinLengthInBins
                + ", permanentInterestBinsFilledPercentage="
                + permanentInterestBinsFilledPercentage + "]";
    }

    public int getPeriodicInterestDistanceInBins() {
        return periodicInterestDistanceInBins;
    }

    public int getPeriodicInterestOccuranceCount() {
        return periodicInterestOccuranceCount;
    }

    public float getPeriodicInterestScoreThreshold() {
        return periodicInterestScoreThreshold;
    }

    public float getPermanentInterestBinsFilledPercentage() {
        return permanentInterestBinsFilledPercentage;
    }

    public int getPermanentInterestOccurenceMinLengthInBins() {
        return permanentInterestOccurenceMinLengthInBins;
    }

    public float getPermanentInterestScoreThreshold() {
        return permanentInterestScoreThreshold;
    }

    public void setPeriodicInterestDistanceInBins(int periodicInterestDistanceInBins) {
        this.periodicInterestDistanceInBins = periodicInterestDistanceInBins;
    }

    public void setPeriodicInterestOccuranceCount(int periodicInterestOccuranceCount) {
        this.periodicInterestOccuranceCount = periodicInterestOccuranceCount;
    }

    public void setPeriodicInterestScoreThreshold(float periodicInterestScoreThreshold) {
        this.periodicInterestScoreThreshold = periodicInterestScoreThreshold;
    }

    public void setPermanentInterestBinsFilledPercentage(float permanentInterestBinsFilledPercentage) {
        this.permanentInterestBinsFilledPercentage = permanentInterestBinsFilledPercentage;
    }

    public void setPermanentInterestOccurenceMinLengthInBins(
            int permanentInterestOccurenceMinLengthInBins) {
        this.permanentInterestOccurenceMinLengthInBins = permanentInterestOccurenceMinLengthInBins;
    }

    public void setPermanentInterestScoreThreshold(float permanentInterestScoreThreshold) {
        this.permanentInterestScoreThreshold = permanentInterestScoreThreshold;
    }
}
