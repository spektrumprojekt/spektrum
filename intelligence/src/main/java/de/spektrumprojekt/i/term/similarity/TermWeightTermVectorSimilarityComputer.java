package de.spektrumprojekt.i.term.similarity;

import de.spektrumprojekt.i.term.weight.TermWeightComputer;

public abstract class TermWeightTermVectorSimilarityComputer implements
        TermVectorSimilarityComputer {
    private final TermWeightComputer termWeightComputer;

    public TermWeightTermVectorSimilarityComputer(TermWeightComputer termWeightComputer) {
        if (termWeightComputer == null) {
            throw new IllegalArgumentException("termWeightComputer cannot be null!");
        }
        this.termWeightComputer = termWeightComputer;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    public TermWeightComputer getTermWeightComputer() {
        return termWeightComputer;
    }

}