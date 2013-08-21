package de.spektrumprojekt.i.timebased;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

public class EnergyCalculationConfiguration implements ConfigurationDescriptable {

    private final int k;
    private final double d;
    private final int G;
    private final NutritionCalculationStrategy strategy;
    private final int historyLength;

    public EnergyCalculationConfiguration(int k, double d, int g, int historyLength,
            NutritionCalculationStrategy strategy) {
        super();
        this.k = k;
        this.d = d;
        G = g;
        this.historyLength = historyLength;
        this.strategy = strategy;
    }

    @Override
    public String getConfigurationDescription() {
        return "EnergyCalculationConfiguration [k=" + k + ", d=" + d + ", G=" + G + ", strategy="
                + strategy.getClass().getName() + "]";
    }

    public double getD() {
        return d;
    }

    public int getG() {
        return G;
    }

    public int getHistoryLength() {
        return historyLength;
    }

    public int getK() {
        return k;
    }

    public NutritionCalculationStrategy getStrategy() {
        return strategy;
    }

}
