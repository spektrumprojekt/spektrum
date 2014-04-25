package de.spektrumprojekt.i.evaluation;

import java.io.IOException;
import java.util.List;

import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;

public interface RatingProvider {

    public List<SpektrumRating> getRatings() throws IOException;
}
