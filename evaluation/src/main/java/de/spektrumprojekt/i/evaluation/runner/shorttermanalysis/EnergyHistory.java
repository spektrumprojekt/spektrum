package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.util.Date;
import java.util.List;

public class EnergyHistory extends TermHistory {

    private EnergyAlgorithm algorithm;

    int historyLength;

    public EnergyHistory(List<Date> startTimes, int historyLength, EnergyAlgorithm algorithm) {
        super(startTimes);
        this.algorithm = algorithm;
        this.historyLength = historyLength;
    }

    public EnergyHistory(String serializedHistory) {
        // TODO this will not work, getName can't work without the algorithm
        super(serializedHistory);
    }

    public void calculateEnergy(TermRecord nutritionRecord) {
        put(algorithm.run(nutritionRecord, historyLength));
    }

    public EnergyAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public String getName() {
        return algorithm.getName() + "_" + historyLength;
    }

    @Override
    protected boolean isRecordOfThisHistory(String string) {
        String[] splitted = string.split(" ");
        if (!Integer.valueOf(splitted[splitted.length - 1]).equals(historyLength)) {
            return false;
        }
        return getName()
                .equals(string.substring(0,
                        string.length() - (splitted[splitted.length - 1].length() + 1)));
    }
}
