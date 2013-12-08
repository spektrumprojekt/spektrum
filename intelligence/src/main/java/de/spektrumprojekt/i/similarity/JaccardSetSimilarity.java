package de.spektrumprojekt.i.similarity;

import java.util.Collection;

import org.apache.commons.collections15.CollectionUtils;

/**
 * 
 * see http://de.wikipedia.org/wiki/Jaccard-Koeffizient
 * 
 * @author Torsten
 * 
 */
public class JaccardSetSimilarity implements SetSimilarity {

    @Override
    public <E> float computeSimilarity(Collection<? extends E> a, Collection<? extends E> b) {

        Collection<E> union = CollectionUtils.union(a, b);

        int bottom = union.size();
        if (bottom == 0) {
            return 0;
        }

        Collection<E> intersection = CollectionUtils.intersection(a, b);

        float top = intersection.size();

        return top / bottom;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

}
