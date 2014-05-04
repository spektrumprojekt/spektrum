package de.spektrumprojekt.i.term.similarity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    public float getSimilarity(Map<Term, UserModelEntry> relevantEntries1,
            Map<Term, UserModelEntry> relevantEntries2) {
        float sumTop = 0;
        float sumBottom = 0;

        for (Term term : relevantEntries1.keySet()) {
            UserModelEntry entry1 = relevantEntries1.get(term);
            UserModelEntry entry2 = relevantEntries2.get(term);

            if (entry2 == null) {
                continue;
            }
            float entryScore1 = entry1.getScoredTerm().getWeight();
            float entryScore2 = entry2.getScoredTerm().getWeight();

            sumTop += entryScore1 * entryScore2;
            sumBottom++;
        }

        if (sumBottom == 0) {
            return 0;
        }
        return sumTop / sumBottom;
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

    @Override
    protected float internalGetSimilarity(
            String messageGroupGlobalId1,
            String messageGroupGlobalId2,
            Map<String, Term> termValuesOfMG1,
            Map<String, Term> termValuesOfMG2,
            Set<String> termsForIteration) {

        float sumTop = 0;
        float sumBottom = 0;

        for (String termValue : termsForIteration) {
            Term t1 = termValuesOfMG1.get(termValue);
            Term t2 = termValuesOfMG2.get(termValue);
            float termWeight1 = t1 == null ? 0 : getTermWeightComputer().determineTermWeight(
                    messageGroupGlobalId1, t1);
            float termWeight2 = t2 == null ? 0 : getTermWeightComputer().determineTermWeight(
                    messageGroupGlobalId2, t2);

            sumTop += termWeight1 * termWeight2;
            sumBottom += Math.max(termWeight1, termWeight2);
        }

        return sumTop / sumBottom;
    }

}
