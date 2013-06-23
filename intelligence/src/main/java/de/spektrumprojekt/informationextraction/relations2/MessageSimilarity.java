package de.spektrumprojekt.informationextraction.relations2;

import de.spektrumprojekt.datamodel.message.Message;

/**
 * <p>
 * Calculate a similarity measure between a pair of {@link Message}s.
 * </p>
 * 
 * @author Philipp Katz
 */
public interface MessageSimilarity {

    float getSimilarity(Message msg1, Message msg2);

}
