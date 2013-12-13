package de.spektrumprojekt.i.similarity.set;

import java.util.Collection;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

/**
 * @author Torsten
 * 
 */
public interface SetSimilarity extends ConfigurationDescriptable {

    public <E> SetSimilarityResult computeSimilarity(final Collection<? extends E> a,
            final Collection<? extends E> b);
}
