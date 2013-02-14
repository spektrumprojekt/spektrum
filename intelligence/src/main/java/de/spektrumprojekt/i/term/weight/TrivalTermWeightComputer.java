package de.spektrumprojekt.i.term.weight;

import de.spektrumprojekt.datamodel.message.Term;

public class TrivalTermWeightComputer implements TermWeightComputer {

    /**
     * {@inheritDoc}
     */
    @Override
    public float determineTermWeight(String messageGroupId, Term term) {
        return 1;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

}
