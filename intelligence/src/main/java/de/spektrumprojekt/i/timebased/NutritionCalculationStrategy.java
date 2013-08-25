package de.spektrumprojekt.i.timebased;

import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.persistence.Persistence;

public interface NutritionCalculationStrategy {

    public float[] getNutrition(UserModelEntry entry, Persistence persistence);

}
