package de.spektrumprojekt.i.evaluation.measure;

import java.util.Map;

import de.spektrumprojekt.i.evaluation.runner.StatsPerUserTimeBin;

public interface MeasureFactory<T extends Measure> {

    public T createMeasure(
            double letTheComputedBePositive,
            double letTheTargetBePositive,
            StatsPerUserTimeBin userTimeBinStats,
            Map<String, Integer> timeBinUserIdToRelevantCount);

    public T createMeasure(String line);

    public String getColumnMeasureHeaders();

    public Class<T> getMeasureClass();

}