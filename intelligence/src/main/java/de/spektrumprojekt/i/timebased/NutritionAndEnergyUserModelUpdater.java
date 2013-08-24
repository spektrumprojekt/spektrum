package de.spektrumprojekt.i.timebased;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.learner.UserModelEntryIntegrationStrategy;
import de.spektrumprojekt.i.learner.time.TimeBinnedUserModelEntryIntegrationStrategy;
import de.spektrumprojekt.i.ranker.RankerConfiguration;
import de.spektrumprojekt.persistence.Persistence;

public class NutritionAndEnergyUserModelUpdater {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(NutritionAndEnergyUserModelUpdater.class);

    private final EnergyCalculationConfiguration energyCalculationConfiguration;

    private final Persistence persistence;

    private final Map<String, UserModelEntryIntegrationStrategy> userModelTypes;

    private Date lastModelCalculationDate;

    private final RankerConfiguration rankerConfiguration;

    private Date firstBinStartTime;

    public NutritionAndEnergyUserModelUpdater(Persistence persistence,
            RankerConfiguration configuration) {
        super();
        this.rankerConfiguration = configuration;
        this.persistence = persistence;
        this.userModelTypes = new HashMap<String, UserModelEntryIntegrationStrategy>();
        for (Entry<String, UserModelEntryIntegrationStrategy> entry : configuration
                .getUserModelTypes().entrySet()) {
            if (needsToBeCalculated(entry.getValue())) {
                userModelTypes.put(entry.getKey(), entry.getValue());
            }
        }
        this.energyCalculationConfiguration = configuration.getEnergyCalculationConfiguration();
        NutritionCalculationStrategy strategy = energyCalculationConfiguration.getStrategy();
        
    }

    public boolean itsTimeToCalculateModels(Date date) {
        if (firstBinStartTime == null) {
            firstBinStartTime = date;
        }
        if (!(date.getTime() - 2
                * rankerConfiguration.getEnergyCalculationConfiguration().getPrecision() > firstBinStartTime
                    .getTime())) {
            return false;
        }
        if (lastModelCalculationDate == null) {
            lastModelCalculationDate = date;
            return true;
        }
        if (date.getTime() - rankerConfiguration.getEnergyCalculationConfiguration().getPrecision() >= lastModelCalculationDate
                .getTime()) {
            lastModelCalculationDate = date;
            return true;
        } else {
            return false;
        }
    }

    private boolean needsToBeCalculated(UserModelEntryIntegrationStrategy entryIntegrationStrategy) {
        return (entryIntegrationStrategy instanceof TimeBinnedUserModelEntryIntegrationStrategy) ? ((TimeBinnedUserModelEntryIntegrationStrategy) entryIntegrationStrategy)
                .isCalculateLater() : false;
    }

    public void updateUserModels() {
        LOGGER.debug("Starting to update UserModels");
        for (String userModelType : userModelTypes.keySet()) {
            Map<UserModel, Collection<UserModelEntry>> userModelsAndEntries = persistence
                    .getAllUserModelEntries(userModelType);
            for (UserModel userModel : userModelsAndEntries.keySet()) {
                LOGGER.debug("working on modeltype {}", userModel.getUserModelType());
                Collection<UserModelEntry> entries = userModelsAndEntries.get(userModel);
                for (UserModelEntry entry : entries) {
                    float weight = 0;
                    float[] nutrition = energyCalculationConfiguration.getStrategy().getNutrition(
                            entry, persistence);
                    int length = nutrition.length - 1;
                    float currentNutrition = nutrition[length];
                    float energy = 0;
                    for (int histNutrIndex = length
                            - energyCalculationConfiguration.getHistoryLength(); histNutrIndex < length; histNutrIndex++) {
                        float historicalNutrition;
                        if (histNutrIndex < 0) {
                            historicalNutrition = 0;
                        } else {
                            historicalNutrition = nutrition[histNutrIndex];
                        }
                        energy += (Math.pow(currentNutrition, 2) - Math.pow(historicalNutrition, 2))
                                / (length - histNutrIndex);
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
        LOGGER.debug("Finished updating UserModels");
    }

}
