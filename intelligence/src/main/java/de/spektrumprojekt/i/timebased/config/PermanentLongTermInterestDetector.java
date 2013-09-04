package de.spektrumprojekt.i.timebased.config;

import de.spektrumprojekt.i.timebased.LongTermInterestDetector;

public class PermanentLongTermInterestDetector implements LongTermInterestDetector {

    private final float permanentInterestScoreThreshold;

    private final int permanentInterestOccurenceMinLengthInBins;

    private final int maxBinsMissingCount;

    public PermanentLongTermInterestDetector(float permanentInterestScoreThreshold,
            int permanentInterestOccurenceMinLengthInBins, float permanentInterestBinsFilledPercentage) {
        super();
        this.permanentInterestScoreThreshold = permanentInterestScoreThreshold;
        this.permanentInterestOccurenceMinLengthInBins = permanentInterestOccurenceMinLengthInBins;
        maxBinsMissingCount = (int) ((1 - permanentInterestBinsFilledPercentage) * permanentInterestOccurenceMinLengthInBins);
    }

    @Override
    public boolean isLongTermInterest(float[] nutrition) {
        int occurenceLength = 0;
        int missingBins = 0;
        for (float currentNutrition : nutrition) {
            if (currentNutrition > permanentInterestScoreThreshold) {
                occurenceLength++;
                if (occurenceLength >= permanentInterestOccurenceMinLengthInBins) {
                    return true;
                }
            } else {
                if (missingBins >= maxBinsMissingCount) {
                    occurenceLength = 0;
                    missingBins = 0;
                } else {
                    missingBins++;
                }
            }
        }
        return false;
    }
}
