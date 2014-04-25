package de.spektrumprojekt.i.evaluation.measure;

import java.util.Map;

import de.spektrumprojekt.commons.output.SpektrumParseableElement;

public abstract class Measure implements SpektrumParseableElement {

    private final double letTheComputedBePositive;
    private final double letTheTargetBePositive;
    private long overallItems;

    public Measure(double letTheComputedBePositive, double letTheTargetBePositive) {
        this.letTheComputedBePositive = letTheComputedBePositive;
        this.letTheTargetBePositive = letTheTargetBePositive;
    }

    /**
     * Adds a evaluator datapoint to compute the measures. Must be in order for average precision
     * and precision-at computations.
     * 
     * @param dataPoint
     */
    public abstract void addDataPoint(EvaluatorDataPoint dataPoint);

    @Override
    public abstract void finalize();

    public abstract Map<String, SpecificMeasure> getFinalMeasures();

    public double getLetTheComputedBePositive() {
        return letTheComputedBePositive;
    }

    public double getLetTheTargetBePositive() {
        return letTheTargetBePositive;
    }

    public long getOverallItems() {
        return overallItems;
    }

    public boolean isComputedPositive(double score) {
        return score >= letTheComputedBePositive;
    }

    public boolean isTargetPositive(double score) {
        return score >= letTheTargetBePositive;
    }

    public void setOverallItems(long overallItems) {
        this.overallItems = overallItems;
    }

    public String toParseableString() {
        return this.toString();
    }
}
