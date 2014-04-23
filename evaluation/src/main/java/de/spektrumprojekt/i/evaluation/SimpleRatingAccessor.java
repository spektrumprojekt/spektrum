package de.spektrumprojekt.i.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;

public class SimpleRatingAccessor implements RatingAccessor {

    private final List<SpektrumRating> trainingRatings;
    private final List<SpektrumRating> testRatings;

    public SimpleRatingAccessor(MessageDataSetProvider dataSetProvider, float trainingThreshold) {

        trainingRatings = new ArrayList<SpektrumRating>();
        testRatings = new ArrayList<SpektrumRating>();

        Map<String, List<SpektrumRating>> user2Ratings = SpektrumRating
                .splitByUsers(dataSetProvider.getRatings());
        for (List<SpektrumRating> userRatings : user2Ratings.values()) {

            Collections.shuffle(userRatings);

            int split = (int) (trainingThreshold * userRatings.size());

            split = Math.min(split, userRatings.size() - 1);
            if (split > 0) {
                trainingRatings.addAll(userRatings.subList(0, split));
            }
            if (split + 1 < userRatings.size()) {
                testRatings.addAll(userRatings.subList(split, userRatings.size()));
            }
        }
    }

    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + " trainingRatingsSize: " + trainingRatings.size()
                + " testRatingsSize: " + testRatings.size();
    }

    public List<SpektrumRating> getTestRatings() {
        return testRatings;
    }

    public List<SpektrumRating> getTrainingRatings() {
        return trainingRatings;
    }

}
