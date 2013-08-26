package de.spektrumprojekt.i.timebased.config;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

public class EnergyCalculationConfiguration implements ConfigurationDescriptable {

    private final float k;
    private final double d;
    private final NutritionCalculationStrategy strategy;
    private final int historyLength;
    private final int nutritionHistLength;

    public EnergyCalculationConfiguration(float k, double d, int historyLength,
            int nutritionHistLength, NutritionCalculationStrategy strategy) {
        super();
        this.k = k;
        this.d = d;
        this.historyLength = historyLength;
        this.strategy = strategy;
        this.nutritionHistLength = nutritionHistLength;
    }

    @Override
    public String getConfigurationDescription() {
        return "EnergyCalculationConfiguration [k=" + k + ", d=" + d + ", strategy="
                + strategy.getClass().getName() + "]";
    }

    public double getD() {
        return d;
    }

    public int getG() {
        return 1;
    }

    public int getHistoryLength() {
        return historyLength;
    }

    public double getK() {
        return k;
    }

    public NutritionCalculationStrategy getStrategy() {
        return strategy;
    }

    public int getNutritionHistLength() {
        return nutritionHistLength;
    }

}
