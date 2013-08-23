package de.spektrumprojekt.i.timebased;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

public class EnergyCalculationConfiguration implements ConfigurationDescriptable {

    private final float k;
    private final double d;
    private final NutritionCalculationStrategy strategy;
    private final int historyLength;
    private long precision;

    public EnergyCalculationConfiguration(float k, double d, int historyLength, long precision,
            NutritionCalculationStrategy strategy) {
        super();
        this.k = k;
        this.d = d;
        this.historyLength = historyLength;
        this.strategy = strategy;
        this.precision = precision;
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

    public long getPrecision() {
        return precision;
    }

    public NutritionCalculationStrategy getStrategy() {
        return strategy;
    }

    public void setPrecision(long precision) {
        this.precision = precision;
    }

}
