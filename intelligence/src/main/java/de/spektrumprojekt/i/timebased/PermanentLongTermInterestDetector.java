package de.spektrumprojekt.i.timebased;

import java.util.LinkedList;
import java.util.List;


public class PermanentLongTermInterestDetector implements LongTermInterestDetector {

    /**
     * potential match
     * 
     */
    private class Track {
        private int occurenceLength = 0;
        private int missingBins = 0;

        public int getMissingBins() {
            return missingBins;
        }

        public int getOccurenceLength() {
            return occurenceLength;
        }

        public void incrementMissingBins() {
            missingBins++;
        }

        public void incrementOccurenceLength() {
            occurenceLength++;
        }

    }

    private final float permanentInterestScoreThreshold;

    private final int permanentInterestOccurenceMinLengthInBins;

    private final int maxBinsMissingCount;

    public PermanentLongTermInterestDetector(float permanentInterestScoreThreshold,
            int permanentInterestOccurenceMinLengthInBins,
            float permanentInterestBinsFilledPercentage) {
        super();
        this.permanentInterestScoreThreshold = permanentInterestScoreThreshold;
        this.permanentInterestOccurenceMinLengthInBins = permanentInterestOccurenceMinLengthInBins;
        maxBinsMissingCount = (int) ((1 - permanentInterestBinsFilledPercentage) * permanentInterestOccurenceMinLengthInBins);
    }

    @Override
    public boolean isLongTermInterest(float[] nutrition) {
        LinkedList<Track> tracks = new LinkedList<Track>();
        for (float currentNutrition : nutrition) {
            if (currentNutrition > permanentInterestScoreThreshold) {
                boolean addNewTrack = true;
                for (Track track : tracks) {
                    if (track.getMissingBins() == 0) {
                        // there is a match without missing bins, so the current occurrence is
                        // tracked
                        addNewTrack = false;
                    }
                }
                // a new track is added, because the nutrition changed from below threshold to above
                // threshold
                if (addNewTrack) {
                    tracks.add(new Track());
                }
                for (Track track : tracks) {
                    track.incrementOccurenceLength();
                    if (track.getOccurenceLength() >= permanentInterestOccurenceMinLengthInBins) {
                        // this track matches all conditions long term interest detected
                        return true;
                    }

                }
            } else {
                List<Track> tracksToRemove = new LinkedList<Track>();
                for (Track track : tracks) {
                    if (track.getMissingBins() >= maxBinsMissingCount) {
                        // to much missing bins, this track is not matching the conditions any more
                        tracksToRemove.add(track);
                    } else {
                        track.incrementMissingBins();
                    }
                }
                tracks.removeAll(tracksToRemove);
            }
        }
        return false;
    }
}
