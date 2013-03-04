package de.spektrumprojekt.i.term.similarity;

import de.spektrumprojekt.i.term.weight.TermWeightComputer;

public abstract class TermWeightTermVectorSimilarityComputer implements
        TermVectorSimilarityComputer {
    private final TermWeightComputer termWeightComputer;

    private final boolean treatMissingUserModelEntriesAsZero;

    public TermWeightTermVectorSimilarityComputer(TermWeightComputer termWeightComputer,
            boolean treatMissingUserModelEntriesAsZero) {
        if (termWeightComputer == null) {
            throw new IllegalArgumentException("termWeightComputer cannot be null!");
        }
        this.termWeightComputer = termWeightComputer;
        this.treatMissingUserModelEntriesAsZero = treatMissingUserModelEntriesAsZero;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + " treatMissingUserModelEntriesAsZero: " + treatMissingUserModelEntriesAsZero
                + " termWeightComputer: " + termWeightComputer.getConfigurationDescription();
    }

    @Override
    public TermWeightComputer getTermWeightComputer() {
        return termWeightComputer;
    }

    public boolean isTreatMissingUserModelEntriesAsZero() {
        return treatMissingUserModelEntriesAsZero;
    }

}