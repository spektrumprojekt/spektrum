package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

public class SumTopFilter extends TopFilter {

    public SumTopFilter(TermHistory history, ShortTermConfiguration configuration) {
        super(history, configuration);
    }

    @Override
    public double calculateScore(TermRecord record) {
        double result = 0;
        for (Double entry : record.getEntries()) {
            result += entry;
        }
        return result;
    }

    @Override
    protected String getFilterMethodName() {
        return "_sum";
    }

}
