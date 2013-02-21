package de.spektrumprojekt.i.term.weight;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Term;

public interface TermWeightComputer extends ConfigurationDescriptable {

    public float determineTermWeight(String messageGroupId, Term term);
}
