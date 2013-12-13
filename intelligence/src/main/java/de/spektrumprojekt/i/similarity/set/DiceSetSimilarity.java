package de.spektrumprojekt.i.similarity.set;

import java.util.Collection;

public class DiceSetSimilarity implements SetSimilarity {

    @Override
    public <E> SetSimilarityResult computeSimilarity(Collection<? extends E> a,
            Collection<? extends E> b) {

        SetSimilarityResult result = new SetSimilarityResult(a, b);

        int bottom = result.getSet1Size() + result.getSet2Size();
        if (bottom > 0) {
            float top = 2 * result.getIntersectionSize();

            result.setSim(top / bottom);
        }

        return result;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

}
