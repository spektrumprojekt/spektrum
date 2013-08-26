package de.spektrumprojekt.i.term.similarity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.term.weight.TermWeightComputer;
import de.spektrumprojekt.i.timebased.MergeValuesStrategy;

public class AverageTermVectorSimilarityComputer extends TermWeightTermVectorSimilarityComputer {

    public AverageTermVectorSimilarityComputer(TermWeightComputer termWeightComputer,
            boolean treatMissingUserModelEntriesAsZero) {
        super(termWeightComputer, treatMissingUserModelEntriesAsZero);
    }

    @Override
    public Float getSimilarity(String messageGroupId,
            Map<String, Map<Term, UserModelEntry>> allEntries, MergeValuesStrategy strategy,
            Collection<Term> terms) {
        float sumTop = 0;
        float sumBottom = 0;
        Collection<Term> termsForIteration;
        if (isTreatMissingUserModelEntriesAsZero()) {
            termsForIteration = terms;
        } else {
            termsForIteration = new HashSet<Term>();
            for (Map<Term, UserModelEntry> userModelEntries : allEntries.values()) {
                termsForIteration.addAll(userModelEntries.keySet());
            }
        }
        for (Term term : termsForIteration) {
            Map<String, Float> entryScores = new HashMap<String, Float>();
            for (String userModelType : allEntries.keySet()) {
                UserModelEntry entry = allEntries.get(userModelType).get(term);
                if (entry != null) {
                    entryScores.put(userModelType, entry.getScoredTerm().getWeight());
                } else if (isTreatMissingUserModelEntriesAsZero()) {
                    entryScores.put(userModelType, 0f);
                }
            }
            float entryScore = strategy.merge(entryScores);
            float termWeight = getTermWeightComputer().determineTermWeight(messageGroupId, term);

            sumTop += termWeight * entryScore;
            sumBottom += termWeight;
        }

        return sumTop / sumBottom;
    }

    @Override
    public float getSimilarity(String messageGroupId, Map<Term, UserModelEntry> relevantEntries,
            Collection<Term> terms) {
        float sumTop = 0;
        float sumBottom = 0;
        Collection<Term> termsForIteration;
        if (isTreatMissingUserModelEntriesAsZero()) {
            termsForIteration = terms;
        } else {
            termsForIteration = relevantEntries.keySet();
        }
        for (Term term : termsForIteration) {
            UserModelEntry entry = relevantEntries.get(term);
            float entryScore = 0;
            if (entry != null) {
                entryScore = entry.getScoredTerm().getWeight();
            }
            float termWeight = getTermWeightComputer().determineTermWeight(messageGroupId, term);

            sumTop += termWeight * entryScore;
            sumBottom += termWeight;
        }

        return sumTop / sumBottom;
    }

}
