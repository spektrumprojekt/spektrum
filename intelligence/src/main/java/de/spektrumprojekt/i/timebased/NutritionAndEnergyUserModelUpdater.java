package de.spektrumprojekt.i.timebased;

import java.util.Collection;
import java.util.Map;

import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.learner.UserModelEntryIntegrationStrategy;
import de.spektrumprojekt.i.learner.time.TimeBinnedUserModelEntryIntegrationStrategy;
import de.spektrumprojekt.i.ranker.RankerConfiguration;
import de.spektrumprojekt.persistence.Persistence;

public class NutritionAndEnergyUserModelUpdater {

    private final EnergyCalculationConfiguration energyCalculationConfiguration;

    private Persistence persistence;

    private final Map<String, UserModelEntryIntegrationStrategy> userModelTypes;

    public NutritionAndEnergyUserModelUpdater(RankerConfiguration configuration) {
        super();
        this.userModelTypes = configuration.getUserModelTypes();
        this.energyCalculationConfiguration = configuration.getEnergyCalculationConfiguration();
    }

    private boolean needsToBeCalculated(UserModelEntryIntegrationStrategy entryIntegrationStrategy) {
        return (entryIntegrationStrategy instanceof TimeBinnedUserModelEntryIntegrationStrategy) ? ((TimeBinnedUserModelEntryIntegrationStrategy) entryIntegrationStrategy)
                .isCalculateLater() : false;
    }

    public void updateUserModels() {
        for (String userModelType : userModelTypes.keySet()) {
            if (needsToBeCalculated(userModelTypes.get(userModelType))) {
                Map<UserModel, Collection<UserModelEntry>> userModelsAndEntries = persistence
                        .getAllUserModelEntries(userModelType);
                for (UserModel userModel : userModelsAndEntries.keySet()) {
                    Collection<UserModelEntry> entries = userModelsAndEntries.get(userModel);
                    for (UserModelEntry entry : entries) {
                        float weight = 0;
                        float[] nutrition = energyCalculationConfiguration.getStrategy()
                                .getNutrition(entry);
                        int length = nutrition.length - 1;
                        float currentNutrition = nutrition[length];
                        float energy = 0;
                        for (int histNutrIndex = length
                                - energyCalculationConfiguration.getHistoryLength(); histNutrIndex < length; histNutrIndex++) {
                            float historicalNutrition = nutrition[histNutrIndex];
                            energy += (Math.pow(currentNutrition, 2) - Math.pow(
                                    historicalNutrition, 2)) / (length - histNutrIndex);
                        }
                        weight = (float) (energyCalculationConfiguration.getG() / (1 + energyCalculationConfiguration
                                .getD()
                                * Math.pow(Math.E, -energyCalculationConfiguration.getK()
                                        * energyCalculationConfiguration.getG() * currentNutrition
                                        * energy)));
                        entry.getScoredTerm().setWeight(weight);
                    }
                    persistence.storeOrUpdateUserModelEntries(userModel, entries);
                }
            }
        }
    }
}
