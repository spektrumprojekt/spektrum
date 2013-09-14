package de.spektrumprojekt.i.timebased;

/**
 * detects long term interests
 * 
 * 
 */
public interface LongTermInterestDetector {

    /**
     * @param nutrition
     *            the scores of the bins in the time to be analyzed
     * @return true if a long term interest was detected
     */
    public boolean isLongTermInterest(float[] nutrition);

}
