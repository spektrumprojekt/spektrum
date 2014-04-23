package de.spektrumprojekt.i.evaluation.runner.crossvalidation;

import java.util.Collection;

import de.spektrumprojekt.i.evaluation.RatingAccessor;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;

public class CrossValidationRatingAccessor implements RatingAccessor {
    private final CrossValidationRatingSplitter splitter;
    private int k;

    public CrossValidationRatingAccessor(CrossValidationRatingSplitter splitter) {
        this.splitter = splitter;
        this.setPartion(0);
    }

    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + "k: " + k
                + " splitter: " + splitter.getConfigurationDescription();
    }

    public Collection<SpektrumRating> getTestRatings() {
        return splitter.getTestRatings(k);
    }

    public Collection<SpektrumRating> getTrainingRatings() {
        return splitter.getTrainingRatings(k);
    }

    public void setPartion(int partion) {
        if (partion >= splitter.getNumberOfPartitions()) {
            throw new IllegalArgumentException(
                    "Partion cannot be larger as available partions. partion=" + partion
                            + " numberOfPartions=" + splitter.getNumberOfPartitions());
        }
        this.k = partion;
    }
}