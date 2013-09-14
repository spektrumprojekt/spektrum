package de.spektrumprojekt.i.timebased;

public class PeriodicLongTermInterestDetector implements LongTermInterestDetector {

    private final float periodicInterestScoreThreshold;
    private final int periodicInterestDistanceInBins;
    private final int periodicInterestOccuranceCount;

    public PeriodicLongTermInterestDetector(float periodicInterestScoreThreshold,
            int periodicInterestDistanceInBins, int periodicInterestOccuranceCount) {
        super();
        this.periodicInterestScoreThreshold = periodicInterestScoreThreshold;
        this.periodicInterestDistanceInBins = periodicInterestDistanceInBins;
        this.periodicInterestOccuranceCount = periodicInterestOccuranceCount;
    }

    @Override
    public boolean isLongTermInterest(float[] nutrition) {
        int occurences = 0;
        int currentDistance = 0;
        for (float currentNutrition : nutrition) {
            if (currentNutrition > periodicInterestScoreThreshold) {
                if (currentDistance >= periodicInterestDistanceInBins || occurences == 0) {
                    occurences++;
                    if (occurences >= periodicInterestOccuranceCount) {
                        return true;
                    }
                } else {
                    currentDistance = 0;
                }
            } else {
                currentDistance++;
            }
        }
        return false;
    }
}
