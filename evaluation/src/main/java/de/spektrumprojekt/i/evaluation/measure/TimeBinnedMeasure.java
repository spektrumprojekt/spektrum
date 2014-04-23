package de.spektrumprojekt.i.evaluation.measure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import de.spektrumprojekt.i.evaluation.runner.StatsPerUserTimeBin;
import de.spektrumprojekt.i.evaluation.runner.StatsPerUserTimeBin.UserTimeBinStats;

/**
 * TODO add option to output this measure to get:<br>
 * - measures per time bin single users<br>
 * - measures per time bin aggregated for user<br>
 * - measures include weighted average, number postive estimated (tp), number missed positive (fn)
 * as tn,fp<br>
 * - option in measure computer to determine best bin for one or two stats and keep it and output<br>
 * 
 * Also check the formular. Do used the threshold for average precision but the order.
 * 
 * 
 */
public class TimeBinnedMeasure extends Measure {

    public final Map<Integer, PrecisionRecallMeasure> timeBinMeasures = new HashMap<Integer, PrecisionRecallMeasure>();

    public final Map<String, Map<Integer, PrecisionRecallMeasure>> userTimeBinMeasures = new HashMap<String, Map<Integer, PrecisionRecallMeasure>>();

    private final Map<String, Integer> timeBinUserIdToRelevantCount;

    private final StatsPerUserTimeBin statsPerUserTimeBin;

    private int minTimeBin = Integer.MAX_VALUE;

    private int maxTimeBin = Integer.MIN_VALUE;
    private DescriptiveStatistics timeBinAveragePrecisionPerUser;

    private double timeBinMeanAveragePrecision;

    private int sumAllRelevantFound;

    private int sumAllRelevantPossible;

    private double precisionAtOfAllTimeBins;

    private int basedOnAdaptedTermCount;

    public TimeBinnedMeasure(double letTheComputedBePositive,
            double letTheTargetBePositive,
            StatsPerUserTimeBin statsPerUserTimeBin,
            Map<String, Integer> timeBinUserIdToRelevantCount) {

        super(letTheComputedBePositive, letTheTargetBePositive);
        this.statsPerUserTimeBin = statsPerUserTimeBin;
        if (statsPerUserTimeBin == null) {
            throw new IllegalArgumentException("statsPerUserTimeBin cannot be null.");
        }
        this.timeBinUserIdToRelevantCount = timeBinUserIdToRelevantCount;
    }

    @Override
    public void addDataPoint(EvaluatorDataPoint dataPoint) {

        PrecisionRecallMeasure overallMeasure = getPrecisionRecallMeasure(timeBinMeasures,
                dataPoint, true);

        Map<Integer, PrecisionRecallMeasure> userTimeBinMeasure = userTimeBinMeasures.get(dataPoint
                .getUserGlobalId());
        if (userTimeBinMeasure == null) {
            userTimeBinMeasure = new HashMap<Integer, PrecisionRecallMeasure>();
            this.userTimeBinMeasures.put(dataPoint.getUserGlobalId(), userTimeBinMeasure);

        }

        PrecisionRecallMeasure userSpecificMeasure = getPrecisionRecallMeasure(userTimeBinMeasure,
                dataPoint, false);

        overallMeasure.addDataPoint(dataPoint);
        userSpecificMeasure.addDataPoint(dataPoint);

        if (dataPoint.isBasedOnAdaptedTerms()) {
            basedOnAdaptedTermCount++;
        }

        minTimeBin = Math.min(minTimeBin, dataPoint.getTimeBin());
        maxTimeBin = Math.max(maxTimeBin, dataPoint.getTimeBin());
    }

    private void computePrecisionAtOnAllTimeBins() {

        int sumAllRelevantFound = 0;
        int sumAllRelevantPossible = 0;

        // for each time bin
        for (int i = minTimeBin; i <= maxTimeBin; i++) {

            // for each user
            for (Entry<String, Map<Integer, PrecisionRecallMeasure>> entry : userTimeBinMeasures
                    .entrySet()) {
                PrecisionRecallMeasure measure = entry.getValue().get(i);
                if (measure != null) {

                    sumAllRelevantFound += measure.positiveReferences();
                    sumAllRelevantPossible += (int) Math.min(measure.total(),
                            measure.getNumberOfAllPositiveRatings());

                }

            }
        }
        if (sumAllRelevantPossible > 0) {
            precisionAtOfAllTimeBins = sumAllRelevantFound / (double) sumAllRelevantPossible;
        }
        this.sumAllRelevantFound = sumAllRelevantFound;
        this.sumAllRelevantPossible = sumAllRelevantPossible;
    }

    private void computeTimeBinMeanAveragePrecision() {
        timeBinMeanAveragePrecision = computeTimeBinMeanAveragePrecision(null);

        timeBinAveragePrecisionPerUser = new DescriptiveStatistics();
        for (String userId : userTimeBinMeasures.keySet()) {
            double userPrecision = computeTimeBinMeanAveragePrecision(userId);

            this.timeBinAveragePrecisionPerUser.addValue(userPrecision);
        }
    }

    private double computeTimeBinMeanAveragePrecision(String userId) {
        // w = omega = number of timebins that have at least one relevant score
        int numTimeBinsWithRelevant = 0;
        // sum of time bins precision
        double timeBinAveragePrecisionSum = 0;

        // for each time bin
        for (int i = minTimeBin; i <= maxTimeBin; i++) {

            // |U_{tb,rel}|
            int numUsersWithRelevant = 0;
            // sum for u in U
            double userAveragePrecisionSum = 0;
            // for each user
            users: for (Entry<String, Map<Integer, PrecisionRecallMeasure>> entry : userTimeBinMeasures
                    .entrySet()) {
                if (userId != null && !userId.equals(entry.getKey())) {
                    continue users;
                }

                PrecisionRecallMeasure timeBinMeasure = entry.getValue().get(i);

                if (timeBinMeasure != null && timeBinMeasure.getNumberOfAllPositiveRatings() > 0) {
                    numUsersWithRelevant++;
                    userAveragePrecisionSum += timeBinMeasure.getAveragePrecision();

                }
            }

            if (numUsersWithRelevant > 0) {
                timeBinAveragePrecisionSum += userAveragePrecisionSum / numUsersWithRelevant;
                numTimeBinsWithRelevant++;
            }
        }

        double weightedMean = 0;
        if (numTimeBinsWithRelevant > 0) {
            weightedMean = timeBinAveragePrecisionSum / numTimeBinsWithRelevant;
        }
        return weightedMean;
    }

    @Override
    public void finalize() {
        this.computeTimeBinMeanAveragePrecision();
        this.computePrecisionAtOnAllTimeBins();
    }

    public int getBasedOnAdaptedTermCount() {
        return basedOnAdaptedTermCount;
    }

    @Override
    public Map<String, SpecificMeasure> getFinalMeasures() {

        List<SpecificMeasure> measures = getSpecificMeasuresList(this.toString());

        return SpecificMeasure.getMap(measures);
    }

    private DescriptiveStatistics getPrecision() {
        DescriptiveStatistics precision = new DescriptiveStatistics();

        for (int i = minTimeBin; i <= maxTimeBin; i++) {
            PrecisionRecallMeasure timeBinMeasure = this.timeBinMeasures.get(i);
            if (timeBinMeasure != null) {
                precision.addValue(timeBinMeasure.precision());
            }
        }
        return precision;
    }

    private PrecisionRecallMeasure getPrecisionRecallMeasure(
            Map<Integer, PrecisionRecallMeasure> measures, EvaluatorDataPoint dataPoint,
            boolean ignoreStats) {
        PrecisionRecallMeasure measure = measures.get(dataPoint.getTimeBin());
        if (measure == null) {
            measure = new PrecisionRecallMeasure(this.getLetTheComputedBePositive(),
                    this.getLetTheTargetBePositive());
            measures.put(dataPoint.getTimeBin(), measure);

            Integer relevantInt = this.timeBinUserIdToRelevantCount.get(dataPoint
                    .getTimeBinUserKey());
            int relevant = relevantInt == null ? 0 : relevantInt.intValue();
            measure.setNumberOfAllPositiveRatings(relevant);

            if (!ignoreStats) {
                UserTimeBinStats stat = this.statsPerUserTimeBin.get(dataPoint.getUserGlobalId(),
                        dataPoint.getTimeBin());
                if (stat == null) {
                    throw new RuntimeException(
                            "stat cannot be null if we got a datapoint for it! dataPoint: "
                                    + dataPoint);
                }

                measure.setNumberOfRatings(stat.getNumberRatings());
            }
        }
        return measure;
    }

    private List<SpecificMeasure> getSpecificMeasuresList(String description) {
        String prefix;
        DescriptiveStatistics precision = getPrecision();

        List<SpecificMeasure> measures = new ArrayList<SpecificMeasure>();

        measures.add(new SpecificMeasure("precisionMean", precision.getMean(), description));
        measures.add(new SpecificMeasure("precisionMax", precision.getMax(), description));
        measures.add(new SpecificMeasure("precisionMin", precision.getMin(), description));
        measures.add(new SpecificMeasure("precisionStandardDeviation", precision
                .getStandardDeviation(), description));
        measures.add(new SpecificMeasure("precisionCount", precision.getN(), description));
        measures.add(new SpecificMeasure("precisionSum", precision.getSum(), description));

        measures.add(new SpecificMeasure("sumAllRelevantFound", sumAllRelevantFound, description));
        measures.add(new SpecificMeasure("sumAllRelevantPossible", sumAllRelevantPossible,
                description));
        measures.add(new SpecificMeasure("precisionAtOfAllTimeBins", precisionAtOfAllTimeBins,
                description));

        prefix = "";
        measures.add(new SpecificMeasure("timeBinMeanAveragePrecision" + prefix,
                this.timeBinMeanAveragePrecision, description));
        measures.add(new SpecificMeasure("averagePrecision" + prefix,
                this.timeBinMeanAveragePrecision, description));
        measures.add(new SpecificMeasure("timeBinAveragePrecisionPerUser25" + prefix,
                this.timeBinAveragePrecisionPerUser.getPercentile(25), description));
        measures.add(new SpecificMeasure("timeBinAveragePrecisionPerUser50" + prefix,
                this.timeBinAveragePrecisionPerUser.getPercentile(50), description));
        measures.add(new SpecificMeasure("timeBinAveragePrecisionPerUser75" + prefix,
                this.timeBinAveragePrecisionPerUser.getPercentile(75), description));
        measures.add(new SpecificMeasure("timeBinAveragePrecisionPerUserMean" + prefix,
                this.timeBinAveragePrecisionPerUser.getMean(), description));
        measures.add(new SpecificMeasure("timeBinAveragePrecisionPerUserMin" + prefix,
                this.timeBinAveragePrecisionPerUser.getMin(), description));
        measures.add(new SpecificMeasure("timeBinAveragePrecisionPerUserMax" + prefix,
                this.timeBinAveragePrecisionPerUser.getMax(), description));

        String str = SpecificMeasure.getString(measures);
        for (SpecificMeasure specificMeasure : measures) {
            specificMeasure.setOthers(specificMeasure.getDescription() + " " + str);
        }

        measures.add(new SpecificMeasure("basedOnAdaptedTermCount", this.basedOnAdaptedTermCount,
                description));

        return measures;
    }

    @Override
    public String toString() {
        List<SpecificMeasure> measures = getSpecificMeasuresList(null);

        StringBuilder sb = new StringBuilder();
        sb.append(this.getLetTheComputedBePositive());
        for (SpecificMeasure specificMeasure : measures) {
            sb.append(" " + specificMeasure.getValue());
        }
        return sb.toString();
    }

}
