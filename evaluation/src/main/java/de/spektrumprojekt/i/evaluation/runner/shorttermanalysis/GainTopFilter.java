package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

public class GainTopFilter extends TopFilter {

    public GainTopFilter(TermHistory history, ShortTermConfiguration configuration) {
        super(history, configuration);
    }

    @Override
    public double calculateScore(TermRecord record) {
        int maxGain = 0;
        int gain = 0;
        int lastValue = 0;
        for (double value : record.getEntries()) {
            if (value >= lastValue) {
                gain += value - lastValue;
            } else {
                gain = 0;
            }
            maxGain = Math.max(gain, maxGain);
        }
        return maxGain;
    }

    @Override
    protected String getFilterMethodName() {
        return "gain";
    }

}
