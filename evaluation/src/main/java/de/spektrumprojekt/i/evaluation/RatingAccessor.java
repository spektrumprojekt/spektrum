package de.spektrumprojekt.i.evaluation;

import java.util.Collection;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;

public interface RatingAccessor extends ConfigurationDescriptable {

    public Collection<SpektrumRating> getTestRatings();

    public Collection<SpektrumRating> getTrainingRatings();
}
