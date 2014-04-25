package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EnergyAlgorithm {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnergyAlgorithm.class);

    public static EnergyAlgorithm[] getAlgorithms() {
        return new EnergyAlgorithm[] {
        // new EnergyAlgorithm() {
        // @Override
        // public String getName() {
        // return "weighted_average";
        // }
        //
        // @Override
        // public TermRecord run(TermRecord nutritionRecord, int historylength) {
        // TermRecord energyRecord = new TermRecord(nutritionRecord.getTerm(),
        // nutritionRecord.getEntries().length);
        // for (int i = 0; i < energyRecord.getEntries().length; i++) {
        // for (int historicalBin = i - historylength; historicalBin <= i; historicalBin++)
        // {
        // double nutrition;
        // if (historicalBin < 0) {
        // nutrition = 0;
        // } else {
        // nutrition = nutritionRecord.getEntry(historicalBin);
        // }
        // double currentEnergy = energyRecord.getEntry(i);
        // int distance = 1 + i - historicalBin;
        // energyRecord.setEntry(i, currentEnergy + (nutrition / distance));
        // }
        // }
        // return energyRecord;
        //
        // }
        // },
        new EnergyAlgorithm() {
            @Override
            public String getName() {
                return "emerging_terms";
            }

            @Override
            public TermRecord run(TermRecord nutritionRecord, int historylength) {
                TermRecord energyRecord = new TermRecord(nutritionRecord.getTerm(),
                        nutritionRecord.getEntries().length);
                for (int i = 0; i < nutritionRecord.getEntries().length - historylength; i++) {
                    int currentBin = historylength + i;
                    for (int historicalBin = i; historicalBin < currentBin; historicalBin++) {
                        Double currentNutrition = nutritionRecord.getEntry(currentBin);
                        Double historicalNutrition = nutritionRecord.getEntry(historicalBin);
                        Double temporaryEnergyValue = energyRecord.getEntry(currentBin);
                        temporaryEnergyValue += (Math.pow(currentNutrition, 2) - Math.pow(
                                historicalNutrition, 2)) / (currentBin - historicalBin);
                        energyRecord.setEntry(currentBin, temporaryEnergyValue);
                    }

                }
                return energyRecord;
            }
        }
        // , new EnergyAlgorithm() {
        // private double[] extendArrayToDouble(double[] array) {
        // double[] result = new double[array.length * 2];
        // for (int i = 0; i < array.length; i++) {
        // result[2 * i] = 2 * array[i];
        // result[2 * i + 1] = 2 * array[i];
        // }
        // return result;
        // }
        //
        // @Override
        // public String getName() {
        // return "engadging_terms_double_precicion";
        // }
        //
        // private double[] mergeArrayToHalf(Double[] array) {
        // double[] result = new double[array.length / 2];
        // for (int i = 0; i < result.length; i++) {
        // result[i] = (array[2 * i] + array[2 * i + 1]) / 2;
        // }
        // return result;
        // }
        //
        // @Override
        // public TermRecord run(TermRecord nutritionRecord, int historylength) {
        // if (historylength % 2 != 0) {
        // LOGGER.warn("history Length should be dividable by 2! Error maybe occure.");
        // }
        // if (nutritionRecord.getEntries().length % 2 != 0) {
        // LOGGER.warn("bin count should be dividable by 2! Error maybe occure.");
        // }
        // TermRecord energyRecord = new TermRecord(nutritionRecord.getTerm(),
        // nutritionRecord.getEntries().length);
        // historylength = historylength / 2;
        // double[] nutritionHinstory = mergeArrayToHalf(nutritionRecord.getEntries());//
        // termEntry.getNutritionHistory());
        // double[] energyHistory = initializeZeroFilledArray(nutritionRecord
        // .getEntries().length / 2 - historylength);
        // // calculate energy
        //
        // for (int i = 0; i < energyHistory.length; i++) {
        // int currentBin = historylength + i;
        // for (int historicalBin = i; historicalBin < currentBin; historicalBin++) {
        // energyHistory[i] += (Math.sqrt(nutritionHinstory[currentBin]) - Math
        // .sqrt(nutritionHinstory[historicalBin]))
        // / (currentBin - historicalBin);
        // }
        //
        // }
        // energyHistory = extendArrayToDouble(energyHistory);
        // for (int i = 0; i < energyHistory.length; i++) {
        // energyRecord.setEntry(i, energyHistory[i]);
        // }
        // return energyRecord;
        // }
        // }
        };
    }

    private static double[] initializeZeroFilledArray(int length) {
        double[] history = new double[length];
        for (int i = 0; i < length; i++) {
            history[i] = 0;
        }
        return history;
    }

    public abstract String getName();

    public abstract TermRecord run(TermRecord nutritionRecord, int historylength);
}
