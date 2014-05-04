package de.spektrumprojekt.i.term.similarity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.spektrumprojekt.commons.time.TimeProviderHolder;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.term.weight.TermWeightComputer;
import de.spektrumprojekt.i.timebased.MergeValuesStrategy;

public class CosinusTermVectorSimilarityComputer extends TermWeightTermVectorSimilarityComputer {

    private TimeDecayFunction timeDecayFunction = null; // TimeDecayFunction.createWithDayCutOff();

    public CosinusTermVectorSimilarityComputer(TermWeightComputer termWeightComputer,
            boolean treatMissingUserModelEntriesAsZero) {
        this(termWeightComputer, null, treatMissingUserModelEntriesAsZero);
    }

    public CosinusTermVectorSimilarityComputer(
            TermWeightComputer termWeightComputer,
            TimeDecayFunction timeDecayFunction,
            boolean treatMissingUserModelEntriesAsZero) {
        super(termWeightComputer, treatMissingUserModelEntriesAsZero);
        this.timeDecayFunction = timeDecayFunction;
    }

    @Override
    public String getConfigurationDescription() {
        return super.getConfigurationDescription() + " " + this.toString();
    }

    private float getEntryWeight(UserModelEntry entry) {
        if (timeDecayFunction == null) {
            return entry.getScoredTerm().getWeight();
        }

        double decay = timeDecayFunction.getDecay(entry.getLastChange().getTime(),
                TimeProviderHolder.DEFAULT.getCurrentTime());
        float sum = entry.getScoreSum() == 0 ? 1 : entry.getScoreSum();
        return Math.max(0, Math.min(1, (float) decay * sum * entry.getScoredTerm().getWeight()));
    }

    @Override
    public float getSimilarity(Map<Term, UserModelEntry> relevantEntries1,
            Map<Term, UserModelEntry> relevantEntries2) {
        float sumTop = 0;
        float squareSum1 = 0;
        float squareSum2 = 0;

        Collection<Term> termsToIterate = new HashSet<Term>();
        termsToIterate.addAll(relevantEntries1.keySet());
        termsToIterate.addAll(relevantEntries2.keySet());

        for (Term term : termsToIterate) {
            UserModelEntry entry1 = relevantEntries1.get(term);
            UserModelEntry entry2 = relevantEntries2.get(term);

            float entryScore1 = entry1 == null ? 0 : getEntryWeight(entry1);
            float entryScore2 = entry2 == null ? 0 : getEntryWeight(entry2);

            sumTop += entryScore1 * entryScore2;
            squareSum1 += entryScore1 * entryScore1;
            squareSum2 += entryScore2 * entryScore2;
        }

        if (squareSum1 * squareSum2 == 0) {
            return 0;
        }
        return (float) (sumTop / Math.sqrt(squareSum1 * squareSum2));
    }

    @Override
    public Float getSimilarity(
            String messageGroupId,
            Map<String, Map<Term, UserModelEntry>> allEntries,
            MergeValuesStrategy strategy,
            Collection<Term> terms) {
        float sumTop = 0;
        float squareSum1 = 0;
        float squareSum2 = 0;
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
                    entryScores.put(userModelType, getEntryWeight(entry));
                } else if (isTreatMissingUserModelEntriesAsZero()) {
                    entryScores.put(userModelType, 0f);
                }
            }
            float entryScore = strategy.merge(entryScores);
            // // UserModelEntry entry = relevantEntries.get(term);
            // // float entryScore = 0;
            // if (entry != null) {
            // entryScore = entry.getScoredTerm().getWeight();
            // }
            float termWeight = getTermWeightComputer().determineTermWeight(messageGroupId, term);

            sumTop += termWeight * entryScore;
            squareSum1 += entryScore * entryScore;
            squareSum2 += termWeight * termWeight;
        }

        if (squareSum1 * squareSum2 == 0) {
            return 0f;
        }
        return (float) (sumTop / Math.sqrt(squareSum1 * squareSum2));
    }

    @Override
    public float getSimilarity(String messageGroupId, Map<Term, UserModelEntry> relevantEntries,
            Collection<Term> terms) {
        float sumTop = 0;
        float squareSum1 = 0;
        float squareSum2 = 0;
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
                entryScore = getEntryWeight(entry);
            }
            float termWeight = getTermWeightComputer().determineTermWeight(messageGroupId, term);

            sumTop += termWeight * entryScore;
            squareSum1 += entryScore * entryScore;
            squareSum2 += termWeight * termWeight;
        }

        if (squareSum1 * squareSum2 == 0) {
            return 0;
        }
        return (float) (sumTop / Math.sqrt(squareSum1 * squareSum2));
    }

    @Override
    protected float internalGetSimilarity(String messageGroupGlobalId1,
            String messageGroupGlobalId2,
            Map<String, Term> termValuesOfMG1, Map<String, Term> termValuesOfMG2,
            Set<String> termsForIteration) {
        float sumTop = 0;
        float squareSum1 = 0;
        float squareSum2 = 0;

        for (String termValue : termsForIteration) {
            Term t1 = termValuesOfMG1.get(termValue);
            Term t2 = termValuesOfMG2.get(termValue);
            float termWeight1 = t1 == null ? 0 : getTermWeightComputer().determineTermWeight(
                    messageGroupGlobalId1, t1);
            float termWeight2 = t2 == null ? 0 : getTermWeightComputer().determineTermWeight(
                    messageGroupGlobalId2, t2);

            sumTop += termWeight2 * termWeight1;
            squareSum1 += termWeight1 * termWeight1;
            squareSum2 += termWeight2 * termWeight2;
        }

        if (squareSum1 * squareSum2 == 0) {
            return 0;
        }
        return (float) (sumTop / Math.sqrt(squareSum1 * squareSum2));
    }

    @Override
    public String toString() {
        return "CosinusTermVectorSimilarityComputer [timeDecayFunction=" + timeDecayFunction + "]";
    }

}
