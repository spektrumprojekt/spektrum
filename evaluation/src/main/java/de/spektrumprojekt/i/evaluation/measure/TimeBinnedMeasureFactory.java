package de.spektrumprojekt.i.evaluation.measure;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import de.spektrumprojekt.i.evaluation.runner.StatsPerUserTimeBin;

public class TimeBinnedMeasureFactory implements MeasureFactory<TimeBinnedMeasure> {

    public TimeBinnedMeasure createMeasure(
            double letTheComputedBePositive,
            double letTheTargetBePositive,
            StatsPerUserTimeBin statsPerUserTimeBin,
            Map<String, Integer> timeBinUserIdToRelevantCount) {

        return new TimeBinnedMeasure(letTheComputedBePositive, letTheTargetBePositive,
                statsPerUserTimeBin, timeBinUserIdToRelevantCount);
    }

    public TimeBinnedMeasure createMeasure(String line) {
        return null;
        // throw new UnsupportedOperationException("not yet implemented.");
    }

    public String getColumnMeasureHeaders() {
        return StringUtils.join(new String[] {
                "limit",
                "precisionMean",
                "precisionMax",
                "precisionMin",
                "precisionStandardDeviation",
                "precisionN",
                "precisionSum" }, " ");
    }

    public Class<TimeBinnedMeasure> getMeasureClass() {
        return TimeBinnedMeasure.class;
    }

}