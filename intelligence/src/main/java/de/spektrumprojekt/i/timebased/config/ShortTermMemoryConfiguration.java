package de.spektrumprojekt.i.timebased.config;

import java.util.HashMap;
import java.util.Map;

public class ShortTermMemoryConfiguration {

    private EnergyCalculationConfiguration energyCalculationConfiguration;

    private MergeValuesStrategy mergeValuesStrategy;

    private long precision;

    private boolean balanceMisingUserModelWeights;

    private final Map<String, Float> raitingWeights = new HashMap<String, Float>();

    public ShortTermMemoryConfiguration(
            EnergyCalculationConfiguration energyCalculationConfiguration,
            MergeValuesStrategy mergeValuesStrategy, long precision) {
        super();
        this.energyCalculationConfiguration = energyCalculationConfiguration;
        this.mergeValuesStrategy = mergeValuesStrategy;
        this.precision = precision;
    }

    public EnergyCalculationConfiguration getEnergyCalculationConfiguration() {
        return energyCalculationConfiguration;
    }

    public MergeValuesStrategy getMergeValuesStrategy() {
        return mergeValuesStrategy;
    }

    public long getPrecision() {
        return precision;
    }

    public Float getRaitingWeight(Object key) {
        return raitingWeights.get(key);
    }

    public Map<String, Float> getRaitingWeights() {
        return raitingWeights;
    }

    public boolean isBalanceMisingUserModelWeights() {
        return balanceMisingUserModelWeights;
    }

    public Float putRatingWeight(String key, Float value) {
        return raitingWeights.put(key, value);
    }

    public void setBalanceMisingUserModelWeights(boolean balanceMisingUserModelWeights) {
        this.balanceMisingUserModelWeights = balanceMisingUserModelWeights;
    }

    public void setEnergyCalculationConfiguration(
            EnergyCalculationConfiguration energyCalculationConfiguration) {
        this.energyCalculationConfiguration = energyCalculationConfiguration;
    }

    public void setMergeValuesStrategy(MergeValuesStrategy mergeValuesStrategy) {
        this.mergeValuesStrategy = mergeValuesStrategy;
    }

    public void setPrecision(long precision) {
        this.precision = precision;
    }

}
