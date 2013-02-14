package de.spektrumprojekt.i.term.similarity;

import java.util.Collection;
import java.util.Map;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModelEntry;

public interface TermVectorSimilarityComputer extends ConfigurationDescriptable {

    public float getSimilarity(
            String messageGroupId,
            Map<Term, UserModelEntry> relevantEntries,
            Collection<Term> terms);
}
