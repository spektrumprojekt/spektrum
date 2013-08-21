package de.spektrumprojekt.i.timebased;

import java.util.Collection;

import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserModelEntryTimeBin;

public class AbsoluteNutritionCalculationStrategy implements NutritionCalculationStrategy {

    @Override
    public float[] getNutrition(UserModelEntry entry) {
        Collection<UserModelEntryTimeBin> timeBinEntries = entry.getTimeBinEntries();
        float[] result = new float[timeBinEntries.size()];
        int i = 0;
        for (UserModelEntryTimeBin bin : timeBinEntries) {
            result[i] = bin.getScoreSum();
        }
        return result;
    }
}
