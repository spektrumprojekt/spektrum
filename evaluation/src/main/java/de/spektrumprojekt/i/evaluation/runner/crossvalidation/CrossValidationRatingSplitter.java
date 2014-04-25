package de.spektrumprojekt.i.evaluation.runner.crossvalidation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;

public class CrossValidationRatingSplitter implements ConfigurationDescriptable {

    protected List<List<SpektrumRating>> partitions;

    private final int n;

    private final boolean switchTrainingTestPartions;

    public CrossValidationRatingSplitter(int n, boolean switchTrainingTestPartions) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be <= 0 but is " + n);
        }
        this.n = n;
        this.switchTrainingTestPartions = switchTrainingTestPartions;

    }

    public CrossValidationRatingSplitter(List<SpektrumRating> ratings) {
        this(ratings, 10, false);
    }

    public CrossValidationRatingSplitter(List<SpektrumRating> ratings, int n,
            boolean switchTrainingTestPartions) {
        this(n, switchTrainingTestPartions);

        this.partitions = getPartitions(ratings);

    }

    private void checkK(int k) {
        if (k >= n || k < 0) {
            throw new IllegalArgumentException("k=" + k + " must be >= 0 and <= n. n=" + n);
        }
    }

    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + " n=" + n
                + " reverse=" + switchTrainingTestPartions;
    }

    public int getMaxSizeOfPartition(int overallValuesCount) {
        int sizeOfPartion = overallValuesCount / n;
        if (sizeOfPartion * n < overallValuesCount) {
            sizeOfPartion += 1;
        }
        return sizeOfPartion;
    }

    public int getNumberOfPartitions() {
        return n;
    }

    public List<List<SpektrumRating>> getPartitions() {
        return partitions;
    }

    protected List<List<SpektrumRating>> getPartitions(List<SpektrumRating> ratings) {
        Collections.shuffle(ratings);
        int maxSizeOfPartition = getMaxSizeOfPartition(ratings.size());
        List<List<SpektrumRating>> partitions = Lists.partition(ratings, maxSizeOfPartition);
        return partitions;
    }

    public Collection<SpektrumRating> getTestRatings(int k) {
        return switchTrainingTestPartions ? internalGetTrainingRatings(k)
                : interalGetTestRatings(k);
    }

    public Collection<SpektrumRating> getTrainingRatings(int k) {
        return switchTrainingTestPartions ? interalGetTestRatings(k)
                : internalGetTrainingRatings(k);
    }

    private List<SpektrumRating> interalGetTestRatings(int k) {
        checkK(k);
        return new ArrayList<SpektrumRating>(partitions.get(k));
    }

    private List<SpektrumRating> internalGetTrainingRatings(int k) {
        checkK(k);
        List<SpektrumRating> trainings = new ArrayList<SpektrumRating>();
        for (int i = 0; i < partitions.size(); i++) {
            if (i != k) {
                trainings.addAll(partitions.get(i));
            }
        }
        return new ArrayList<SpektrumRating>(trainings);
    }

}
