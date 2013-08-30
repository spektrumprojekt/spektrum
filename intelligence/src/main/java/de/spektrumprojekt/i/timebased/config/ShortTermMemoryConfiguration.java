package de.spektrumprojekt.i.timebased.config;

import java.util.HashMap;
import java.util.Map;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.user.UserModel;

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
                + energyCalculationConfiguration.getConfigurationDescription() + "]";
    }

    public EnergyCalculationConfiguration getEnergyCalculationConfiguration() {
        return energyCalculationConfiguration;
    }

    public MergeValuesStrategy getMergeValuesStrategy() {
        return mergeValuesStrategy;
    }

    /**
     * the length of one bin of the time binned model, also the period between recalculating the
     * short term user model
     * 
     * @return period length in ms
     */
    public long getPrecision() {
        return precision;
    }

    /**
     * the weight of a {@link UserModel} instance for rating messages
     * 
     * @param key
     * @return
     */
    public Float getRaitingWeight(Object key) {
        return raitingWeights.get(key);
    }

    public Map<String, Float> getRaitingWeights() {
        return raitingWeights;
    }

    /**
     * if a weight is missing the other weights are scaled to be 1 in sum.
     * 
     * @return
     */
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
