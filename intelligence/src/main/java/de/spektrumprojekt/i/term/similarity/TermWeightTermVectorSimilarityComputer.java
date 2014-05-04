package de.spektrumprojekt.i.term.similarity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.spektrumprojekt.datamodel.message.Term;
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

    protected Map<String, Term> getMGFreeTermValues(Collection<Term> termsOfMG) {
        Map<String, Term> valuesToTerms = new HashMap<String, Term>();
        for (Term t : termsOfMG) {
            valuesToTerms.put(t.extractMessageGroupFreeTermValue(), t);
        }
        return valuesToTerms;
    }

    public float getSimilarity(
            String messageGroupGlobalId1,
            String messageGroupGlobalId2,
            Collection<Term> termsOfMG1,
            Collection<Term> termsOfMG2) {

        if (termsOfMG1.size() == 0 || termsOfMG2.size() == 0) {
            return 0;
        }

        Map<String, Term> termValuesOfMG1 = getMGFreeTermValues(termsOfMG1);
        Map<String, Term> termValuesOfMG2 = getMGFreeTermValues(termsOfMG2);

        Set<String> termsForIteration = new HashSet<String>();
        termsForIteration.addAll(termValuesOfMG1.keySet());
        termsForIteration.addAll(termValuesOfMG2.keySet());

        return internalGetSimilarity(messageGroupGlobalId1, messageGroupGlobalId2, termValuesOfMG1,
                termValuesOfMG2, termsForIteration);
    }

    @Override
    public TermWeightComputer getTermWeightComputer() {
        return termWeightComputer;
    }

    protected abstract float internalGetSimilarity(String messageGroupGlobalId1,
            String messageGroupGlobalId2,
            Map<String, Term> termValuesOfMG1, Map<String, Term> termValuesOfMG2,
            Set<String> termsForIteration);

    public boolean isTreatMissingUserModelEntriesAsZero() {
        return treatMissingUserModelEntriesAsZero;
    }

}