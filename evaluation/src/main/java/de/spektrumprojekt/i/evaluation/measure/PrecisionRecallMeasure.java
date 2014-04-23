package de.spektrumprojekt.i.evaluation.measure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class PrecisionRecallMeasure extends Measure {

    public static String getHeader() {
        return StringUtils.join(new String[] {
                "limit",
                "precision",
                "recall",
                "f1Score",
                "f2Score",
                "dice",
                "tp",
                "fp",
                "fn",
                "tn",
                "ratioPositiveResponses",
                "ratioRositiveReferences",
                "numberOfRatings",
                "numberOfAllPositiveRatings",
                "averagePrecision"
        }, " ");
    }

    private long tp;

    private long fp;

    private long fn;

    private long tn;

    private double averagePrecisionSum;
    private long numberOfRatings;
    private long numberOfAllPositiveRatings;

    public PrecisionRecallMeasure(double letTheComputedBePositive, double letTheTargetBePositive) {
        super(letTheComputedBePositive, letTheTargetBePositive);
    }

    public double accuracy() {
        if (total() == 0) {
            return 0;
        }
        return correct() / (double) total();
    }

    @Override
    public void addDataPoint(EvaluatorDataPoint dataPoint) {
        boolean isRelevant = false;
        if (isTargetPositive(dataPoint.getTarget())) {
            isRelevant = true;
            if (isComputedPositive(dataPoint.getComputed())) {
                // true positive
                tp++;

            } else {
                // missing negative
                fn++;
            }
        } else {
            if (isComputedPositive(dataPoint.getComputed())) {
                // false positive
                fp++;
            } else {
                // match negative
                tn++;
            }
        }
        // isRelevant: relevant(score_{u,tb})
        if (isRelevant) {
            // min = min (k, |relevant_1(ratings_{scores_{u,tb}})|) = number of overall existing
            // relevant messages
            double min = Math.min(total(), this.numberOfAllPositiveRatings);

            if (min > 0) {
                // precisionAt = P@i = all relevant message returned / min
                double precisionAt = positiveReferences() / min;
                averagePrecisionSum += precisionAt;
            }
        }

    }

    public long correct() {
        return tp + tn;
    }

    public double dice() {
        return 2.0 * tp / (positiveReferences() + positiveResponses());
    }

    public double f1Score() {
        return fScore(1);
    }

    @Override
    public void finalize() {

    }

    public double fScore(double beta) {
        if (precision() + recall() == 0) {
            return 0;
        }
        return (1 + beta * beta) * (precision() * recall())
                / (beta * beta * precision() + recall());
    }

    public double getAveragePrecision() {
        // min(k, number of relevant ratings that exist
        double min = Math.min(total(), this.numberOfAllPositiveRatings);
        double ap = 0;
        if (min > 0) {
            ap = averagePrecisionSum / min;
        }
        return ap;
    }

    @Override
    public Map<String, SpecificMeasure> getFinalMeasures() {
        List<SpecificMeasure> measures = new ArrayList<SpecificMeasure>();
        measures.add(new SpecificMeasure("f1score", this.f1Score(), this.toString()));
        measures.add(new SpecificMeasure("f2score", this.fScore(2d), this.toString()));
        measures.add(new SpecificMeasure("precision", this.precision(), this.toString()));
        measures.add(new SpecificMeasure("recall", this.recall(), this.toString()));
        measures.add(new SpecificMeasure("dice", this.dice(), this.toString()));
        measures.add(new SpecificMeasure("positiveResponses", this.ratioPositiveResponses(), this
                .toString()));
        measures.add(new SpecificMeasure("positiveReferences", this.ratioPositiveReferences(), this
                .toString()));
        measures.add(new SpecificMeasure("numberOfRatings", numberOfRatings, this
                .toString()));
        measures.add(new SpecificMeasure("numberOfAllPositiveRatings", numberOfAllPositiveRatings,
                this
                        .toString()));
        measures.add(new SpecificMeasure("averagePrecision", getAveragePrecision(), this
                .toString()));

        return SpecificMeasure.getMap(measures);
    }

    public long getNumberOfAllPositiveRatings() {
        if (this.positiveReferences() > this.numberOfAllPositiveRatings) {
            throw new RuntimeException("This cannot be positiveReferences: "
                    + this.positiveReferences() + " > numberOfAllPositiveRatings "
                    + numberOfAllPositiveRatings);
        }
        return numberOfAllPositiveRatings;
    }

    public long getNumberOfRatings() {
        return numberOfRatings;
    }

    public long positiveReferences() {
        return tp + fn;
    }

    public long positiveResponses() {
        return tp + fp;
    }

    public double precision() {
        if (positiveResponses() == 0) {
            return 0;
        }
        return tp / (double) positiveResponses();

    }

    public double precisionAt() {
        double min = Math.min(this.total(), this.numberOfAllPositiveRatings);
        if (min == 0) {
            return 0;
        }
        return this.positiveReferences() / min;
    }

    public double ratioPositiveReferences() {
        return getOverallItems() == 0 ? 0 : positiveReferences() / (double) getOverallItems();
    }

    public double ratioPositiveResponses() {
        return getOverallItems() == 0 ? 0 : positiveResponses() / (double) getOverallItems();
    }

    public double recall() {
        if (tp + fn == 0) {
            return 0;
        }
        return tp / (double) (tp + fn);
    }

    public void setNumberOfAllPositiveRatings(long numberOfAllPositiveRatings) {
        this.numberOfAllPositiveRatings = numberOfAllPositiveRatings;
    }

    public void setNumberOfRatings(long numberOfRatings) {
        this.numberOfRatings = numberOfRatings;
    }

    @Override
    public String toString() {
        return StringUtils.join(new Object[] {
                this.getLetTheComputedBePositive(),
                precision(),
                recall(),
                f1Score(),
                fScore(2d),
                dice(),
                tp,
                fp,
                fn,
                tn,
                ratioPositiveResponses(),
                ratioPositiveReferences(),
                numberOfRatings,
                numberOfAllPositiveRatings,
                getAveragePrecision()
        }, " ");

    }

    public long total() {
        return tp + fp + fn + tn;
    }
}