package de.spektrumprojekt.i.similarity.set;

import java.util.Collection;

import org.apache.commons.collections15.CollectionUtils;

public class DiceSetSimilarity implements SetSimilarity {

    @Override
    public <E> float computeSimilarity(Collection<? extends E> a, Collection<? extends E> b) {

        int bottom = a.size() + b.size();
        if (bottom == 0) {
            return 0;
        }

        Collection<E> intersection = CollectionUtils.intersection(a, b);

        float top = 2 * intersection.size();

        return top / bottom;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

}
