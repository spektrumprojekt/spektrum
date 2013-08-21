package de.spektrumprojekt.i.timebased;

import de.spektrumprojekt.datamodel.user.UserModelEntry;

public interface NutritionCalculationStrategy {

    public float[] getNutrition(UserModelEntry entry);

}
