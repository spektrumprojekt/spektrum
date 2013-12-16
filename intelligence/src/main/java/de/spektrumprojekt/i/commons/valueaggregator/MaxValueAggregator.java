package de.spektrumprojekt.i.commons.valueaggregator;

import de.spektrumprojekt.i.learner.adaptation.ValueAggregator;

public class MaxValueAggregator implements ValueAggregator {

    private double max = Double.NaN;

    private final boolean ignoreWeights;

    public MaxValueAggregator() {
        this.ignoreWeights = true;
    }

    public MaxValueAggregator(boolean ignoreWeights) {
        this.ignoreWeights = ignoreWeights;
    }

    @Override
    public void add(double value, double countWeight) {
        if (!ignoreWeights) {
            value *= countWeight;
        }
        if (Double.isNaN(max)) {
            max = value;
        } else {
            max = Math.max(max, value);
        }
    }

    @Override
    public double getValue() {
        return max;
    }

}
