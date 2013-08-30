package de.spektrumprojekt.i.timebased.config;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

public class EnergyCalculationConfiguration implements ConfigurationDescriptable {

    private final float k;
    private final double d;
    private final NutritionCalculationStrategy strategy;
    private final int energyHistoryLength;
    private final int nutritionHistLength;
    private final int binAggregationCount;

    public EnergyCalculationConfiguration(float k, double d, int energyHistoryLength,
            int nutritionHistLength, NutritionCalculationStrategy strategy, int binAggregationCount) {
        super();
        this.k = k;
        this.d = d;
        this.energyHistoryLength = energyHistoryLength;
        this.strategy = strategy;
        this.nutritionHistLength = nutritionHistLength;
        this.binAggregationCount = binAggregationCount;
    }

    @Override
    public String getConfigurationDescription() {
        return "EnergyCalculationConfiguration [k=" + k + ", d=" + d + ", strategy=" + strategy
                + ", energyHistoryLength=" + energyHistoryLength + ", nutritionHistLength"
                + nutritionHistLength + "]";
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

    @Override
    public String toString() {
        return getConfigurationDescription();
    }

    public int getBinAggregationCount() {
        return binAggregationCount;
    }

}
