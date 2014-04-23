package de.spektrumprojekt.i.evaluation.runner.aggregator.compare;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AveragePrecisionRunResult {

    private final EvaluationRunResult evaluationRunResult;

    private final static Set<String> VALUE_NAMES = new HashSet<String>(Arrays.asList(new String[] {
            "averagePrecisionWeightedPerUser",
            "averagePrecisionPerUser",
            "averagePrecision25PerUser",
            "averagePrecision50PerUser",
            "averagePrecision75PerUser",
            "averagePrecisionMeanPerUser",
            "averagePrecisionMinPerUser",
            "averagePrecisionMaxPerUser"
    }));

    public AveragePrecisionRunResult(String title) {
        evaluationRunResult = new EvaluationRunResult();
        evaluationRunResult.setTitle(title);
    }

    public boolean containsName(String name) {
        return VALUE_NAMES.contains(name);
    }

    public EvaluationRunResult getEvaluationRunResult() {
        return evaluationRunResult;
    }

    public void setValue(String name, double value) {
        if (name.startsWith("averagePrecisionWeightedPerUser")) {
            evaluationRunResult.setMean(value);
        } else if (name.startsWith("averagePrecisionPerUser")) {
        } else if (name.startsWith("averagePrecision25PerUser")) {
            evaluationRunResult.setQuartile1st(value);
        } else if (name.startsWith("averagePrecision50PerUser")) {
            evaluationRunResult.setMedian(value);
        } else if (name.startsWith("averagePrecision75PerUser")) {
            evaluationRunResult.setQuartile3rd(value);
        } else if (name.startsWith("averagePrecisionMeanPerUser")) {

        } else if (name.startsWith("averagePrecisionMinPerUser")) {
            evaluationRunResult.setMin(value);
        } else if (name.startsWith("averagePrecisionMaxPerUser")) {
            evaluationRunResult.setMax(value);
        }
    }
}