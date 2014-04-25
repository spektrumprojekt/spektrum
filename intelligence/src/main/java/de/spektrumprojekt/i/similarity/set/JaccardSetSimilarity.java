package de.spektrumprojekt.i.similarity.set;

import java.util.Collection;

/**
 * 
 * see http://de.wikipedia.org/wiki/Jaccard-Koeffizient
 * 
 * @author Torsten
 * 
 */
public class JaccardSetSimilarity implements SetSimilarity {

    @Override
    public <E> SetSimilarityResult computeSimilarity(Collection<? extends E> a,
            Collection<? extends E> b) {

        SetSimilarityResult result = new SetSimilarityResult(a, b);

        float top = result.getIntersectionSize();
        int bottom = result.getUnionSize();

        if (bottom > 0) {
            result.setSim(top / bottom);
        }
        return result;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

}
