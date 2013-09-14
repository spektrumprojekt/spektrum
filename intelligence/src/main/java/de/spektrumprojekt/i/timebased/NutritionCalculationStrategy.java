package de.spektrumprojekt.i.timebased;

import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.persistence.Persistence;

/**
 * This strategy is used to calculate the nutrition
 * 
 * 
 * @author lub
 * 
 */
public interface NutritionCalculationStrategy {

    /**
     * 
     * @param entry
     * @param persistence
     *            can be used for additional information
     * @return nutrition
     */
    public float[] getNutrition(UserModelEntry entry, Persistence persistence);

}
