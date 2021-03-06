package de.spektrumprojekt.i.timebased.config;

import java.util.HashMap;
import java.util.Map;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

public class ShortTermMemoryConfiguration implements ConfigurationDescriptable {

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

    @Override
    public String getConfigurationDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Map.Entry<String, Float> entry : raitingWeights.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(entry.getKey());
            sb.append(":");
            sb.append(entry.getValue());
        }
        sb.append("]");
        return "ShortTermMemoryConfiguration [mergeValuesStrategy=" + mergeValuesStrategy
                + ", precision=" + precision + ", balanceMisingUserModelWeights="
                + balanceMisingUserModelWeights + ", raitingWeights=" + sb.toString()
                + ", energyCalculationConfiguration="
                + (energyCalculationConfiguration == null ? "null" :
                        energyCalculationConfiguration.getConfigurationDescription()) + "]";
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

    @Override
    public String toString() {
        return getConfigurationDescription();
    }

}
