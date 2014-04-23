package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

public class MaxTopFilter extends TopFilter {

    public MaxTopFilter(TermHistory history, ShortTermConfiguration configuration) {
        super(history, configuration);
    }

    @Override
    public double calculateScore(TermRecord record) {
        return max(record.getEntries());
    }

    @Override
    protected String getFilterMethodName() {
        return "max";
    }

    private double max(Double[] entries) {
        double max = 0;
        for (double entry : entries) {
            max = Math.max(max, entry);
        }
        return max;
    }
}
