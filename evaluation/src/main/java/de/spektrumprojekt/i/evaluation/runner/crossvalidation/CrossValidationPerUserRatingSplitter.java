package de.spektrumprojekt.i.evaluation.runner.crossvalidation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;

public class CrossValidationPerUserRatingSplitter extends CrossValidationRatingSplitter {

    public CrossValidationPerUserRatingSplitter(List<SpektrumRating> ratings) {
        this(ratings, 10, false);

    }

    public CrossValidationPerUserRatingSplitter(List<SpektrumRating> ratings, int n) {
        this(ratings, n, false);
    }

    public CrossValidationPerUserRatingSplitter(List<SpektrumRating> ratings, int n,
            boolean switchTrainingTestPartions) {
        super(n, switchTrainingTestPartions);

        Map<String, List<SpektrumRating>> userRatings = SpektrumRating.splitByUsers(ratings);

        this.partitions = new ArrayList<List<SpektrumRating>>(n);
        for (int i = 0; i < n; i++) {
            this.partitions.add(new ArrayList<SpektrumRating>());
        }

        for (Entry<String, List<SpektrumRating>> entry : userRatings.entrySet()) {

            List<List<SpektrumRating>> userPartitions = getPartitions(entry.getValue());

            for (int i = 0; i < userPartitions.size(); i++) {
                this.partitions.get(i).addAll(userPartitions.get(i));
            }
        }

    }

}
