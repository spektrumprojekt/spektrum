package de.spektrumprojekt.i.evaluation.measure;

import java.util.Map;

import de.spektrumprojekt.i.evaluation.runner.StatsPerUserTimeBin;

public class PrecisionRecallMeasureFactory implements MeasureFactory<PrecisionRecallMeasure> {

    public PrecisionRecallMeasure createMeasure(
            double letTheComputedBePositive,
            double letTheTargetBePositive,
            StatsPerUserTimeBin statsPerUserTimeBin,
            Map<String, Integer> timeBinUserIdToRelevantCount) {

        return new PrecisionRecallMeasure(letTheComputedBePositive, letTheTargetBePositive);
    }

    public PrecisionRecallMeasure createMeasure(String line) {
        return null;
        // throw new UnsupportedOperationException("not yet implemented.");
    }

    public String getColumnMeasureHeaders() {
        return PrecisionRecallMeasure.getHeader();
    }

    public Class<PrecisionRecallMeasure> getMeasureClass() {
        return PrecisionRecallMeasure.class;
    }

}