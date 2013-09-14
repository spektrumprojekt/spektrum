package de.spektrumprojekt.i.timebased.config;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

public class EnergyCalculationConfiguration implements ConfigurationDescriptable {

    private float k;
    private double d;
    private NutritionCalculationStrategy strategy;
    private int energyHistoryLength;
    private int nutritionHistLength;

    private final int binAggregationCount;

    public EnergyCalculationConfiguration(
            float k,
            double d,
            int energyHistoryLength,
            int nutritionHistLength,
            NutritionCalculationStrategy strategy,
            int binAggregationCount) {

        this.k = k;
        this.d = d;
        this.energyHistoryLength = energyHistoryLength;
        this.strategy = strategy;
        this.nutritionHistLength = nutritionHistLength;
        this.binAggregationCount = binAggregationCount;
    }

    public int getBinAggregationCount() {
        return binAggregationCount;
    }

    @Override
    public String getConfigurationDescription() {
        return toString();
    }

    public double getD() {
        return d;
    }

    public int getEnergyHistoryLength() {
        return energyHistoryLength;
    }

    public int getG() {
        return 1;
    }

    public double getK() {
        return k;
    }

    public int getNutritionHistLength() {
        return nutritionHistLength;
    }

    public NutritionCalculationStrategy getStrategy() {
        return strategy;
    }

    public void setD(double d) {
        this.d = d;
    }

    public void setEnergyHistoryLength(int energyHistoryLength) {
        this.energyHistoryLength = energyHistoryLength;
    }

    public void setK(float k) {
        this.k = k;
    }

    public void setNutritionHistLength(int nutritionHistLength) {
        this.nutritionHistLength = nutritionHistLength;
    }

    public void setStrategy(NutritionCalculationStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public String toString() {
        return "EnergyCalculationConfiguration [k=" + k + ", d=" + d + ", strategy=" + strategy
                + ", energyHistoryLength=" + energyHistoryLength + ", nutritionHistLength="
                + nutritionHistLength + ", binAggregationCount=" + binAggregationCount + "]";
    }

}
