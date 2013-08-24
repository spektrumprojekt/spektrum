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
import de.spektrumprojekt.i.ranker.RankerConfiguration;
import de.spektrumprojekt.i.ranker.UserModelConfiguration;
import de.spektrumprojekt.i.timebased.config.ShortTermMemoryConfiguration;
import de.spektrumprojekt.persistence.Persistence;

public class NutritionAndEnergyUserModelUpdater {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(NutritionAndEnergyUserModelUpdater.class);

    private final ShortTermMemoryConfiguration shortTermMemoryConfiguration;

    private final Persistence persistence;

    private final Map<String, UserModelConfiguration> userModelTypes;

    private Date lastModelCalculationDate;

    private final RankerConfiguration rankerConfiguration;

    private Date firstBinStartTime;

    private final double d;

    private final double k;

    private final double G;

    NutritionCalculationStrategy strategy;

    public NutritionAndEnergyUserModelUpdater(Persistence persistence,
            RankerConfiguration configuration) {
        super();
        this.rankerConfiguration = configuration;
        this.persistence = persistence;
        this.userModelTypes = new HashMap<String, UserModelConfiguration>();
        for (Entry<String, UserModelConfiguration> entry : configuration.getUserModelTypes()
                .entrySet()) {
            if (needsToBeCalculated(entry.getValue())) {
                userModelTypes.put(entry.getKey(), entry.getValue());
            }
        }
        this.shortTermMemoryConfiguration = configuration.getShortTermMemoryConfiguration();
        // NutritionCalculationStrategy strategy = energyCalculationConfiguration.getStrategy();
        d = shortTermMemoryConfiguration.getEnergyCalculationConfiguration().getD();
        k = shortTermMemoryConfiguration.getEnergyCalculationConfiguration().getK();
        G = shortTermMemoryConfiguration.getEnergyCalculationConfiguration().getG();
        switch (shortTermMemoryConfiguration.getEnergyCalculationConfiguration().getStrategy()) {
        case RELATIVE:
            strategy = new RelativeNutritionCalculationStrategy();
            break;
        case ABSOLUTE:
            strategy = new AbsoluteNutritionCalculationStrategy();
            break;
        }
    }

    public boolean itsTimeToCalculateModels(Date date) {
        if (firstBinStartTime == null) {
            firstBinStartTime = date;
        }
        if (!(date.getTime() - 2
                * rankerConfiguration.getShortTermMemoryConfiguration().getPrecision() > firstBinStartTime
                    .getTime())) {
            return false;
        }
        if (lastModelCalculationDate == null) {
            lastModelCalculationDate = date;
            return true;
        }
        if (date.getTime() - rankerConfiguration.getShortTermMemoryConfiguration().getPrecision() >= lastModelCalculationDate
                .getTime()) {
            lastModelCalculationDate = date;
            return true;
        } else {
            return false;
        }
    }

    private boolean needsToBeCalculated(UserModelConfiguration entryIntegrationStrategy) {
        return (entryIntegrationStrategy.getUserModelEntryIntegrationStrategy()
                .equals(UserModelConfiguration.UserModelEntryIntegrationStrategy.TIMEBINNED)) ? entryIntegrationStrategy
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
                    float[] nutrition = strategy.getNutrition(entry, persistence);
                    int length = nutrition.length - 1;
                    float currentNutrition = nutrition[length];
                    float energy = 0;
                    for (int histNutrIndex = length
                            - shortTermMemoryConfiguration.getEnergyCalculationConfiguration()
                                    .getHistoryLength(); histNutrIndex < length; histNutrIndex++) {
                        float historicalNutrition;
                        if (histNutrIndex < 0) {
                            historicalNutrition = 0;
                        } else {
                            historicalNutrition = nutrition[histNutrIndex];
                        }
                        energy += (Math.pow(currentNutrition, 2) - Math.pow(historicalNutrition, 2))
                                / (length - histNutrIndex);
                    }
                    weight = (float) (G / (1 + d
                            * Math.pow(Math.E, -k * G * currentNutrition * energy)));
                    entry.getScoredTerm().setWeight(weight);
                }
                persistence.storeOrUpdateUserModelEntries(userModel, entries);
            }
        }
        LOGGER.debug("Finished updating UserModels");
    }

}
