package de.spektrumprojekt.i.timebased;

import java.util.Collection;
import java.util.Map;

import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.persistence.Persistence;

public class NutritionAndEnergyUserModelUpdater {

    private final int G = 1;

    private Persistence persistence;

    private NutritionCalculationStrategy strategy;

    private float d;

    private float k;

    private int historyLength;

    private final String[] usermodels = new String[] {};

    // TODO üarameters

    public void updateUserModels() {
        for (String userModelType : usermodels) {
            Map<UserModel, Collection<UserModelEntry>> userModelsAndEntries = persistence
                    .getAllUserModelEntries(userModelType);
            for (UserModel userModel : userModelsAndEntries.keySet()) {
                Collection<UserModelEntry> entries = userModelsAndEntries.get(userModel);
                for (UserModelEntry entry : entries) {
                    float weight = 0;
                    float[] nutrition = strategy.getNutrition(entry);
                    int length = nutrition.length - 1;
                    float currentNutrition = nutrition[length];
                    float energy = 0;
                    for (int histNutrIndex = length - historyLength; histNutrIndex < length; histNutrIndex++) {
                        float historicalNutrition = nutrition[histNutrIndex];
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
    }
}
