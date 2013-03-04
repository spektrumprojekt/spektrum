package de.spektrumprojekt.informationextraction.extractors;

import java.util.Collection;

import de.spektrumprojekt.datamodel.message.Term;

/**
 * <p>
 * Implementations of this interface provide a predefined vocabulary of tags or terms.
 * </p>
 * 
 * @author Philipp Katz
 */
public interface TagSource {

    public Collection<Term> getTags();

}
