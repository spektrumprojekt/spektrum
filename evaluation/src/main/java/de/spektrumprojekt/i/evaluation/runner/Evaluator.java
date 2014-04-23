package de.spektrumprojekt.i.evaluation.runner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.i.evaluation.measure.EvaluatorDataPoint;
import de.spektrumprojekt.i.evaluation.runner.processor.EvaluationException;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.ranker.feature.FeatureAggregate;
import de.spektrumprojekt.persistence.Persistence;

public class Evaluator {

    // private final static Logger EVAL_LOGGER = LoggerFactory.getLogger("EVAL-LOGGER");

    private Persistence persistence;
    private int count;
    private int unable;
    private float error;

    private float sqrError;

    private float meanError;

    private final Writer writer;

    private double rmse;

    private int currentCount;

    private int basedOnAdaptedTermsCount;

    public Evaluator(String resultFilename) throws IOException {

        this.writer = new BufferedWriter(new FileWriter(new File(resultFilename)));

        addConfig(EvaluatorDataPoint.getColumnHeaders());
        addConfig(EvaluatorDataPoint.getFileDescription());
    }

    public void addConfig(String text) {
        try {
            writer.write("# " + text + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // EVAL_LOGGER.info("# " + text);
    }

    public void close() {
        IOUtils.closeQuietly(writer);
    }

    private void consolidate() {
        int n = count - unable;
        if (n > 0) {
            meanError = error / n;
            rmse = Math.sqrt(sqrError / n);
        }
    }

    /**
     * TODO also write the feature matrix, that is the value of each feature per rating (or optional
     * into a separate file?)
     * 
     * @param goal
     * @param context
     * @throws IOException
     */
    public void evaluate(UserMessageScore goal, UserMessageScore computed,
            MessageFeatureContext context)
            throws EvaluationException {
        if (goal == null) {
            throw new IllegalArgumentException("goal cannot be null!");
        }
        currentCount++;
        count++;
        if (computed == null) {
            unable++;
        } else {
            float diff = Math.abs(goal.getScore() - computed.getScore());
            error += diff;
            sqrError += diff * diff;
            consolidate();

            if (computed.isBasedOnAdaptedTerms()) {
                basedOnAdaptedTermsCount++;
            }
        }

        FeatureAggregate features = context.getUserContext(goal.getUserGlobalId())
                .getFeatureAggregate();

        EvaluatorDataPoint dataPoint = new EvaluatorDataPoint();
        if (computed == null) {
            dataPoint.setUnavailable(true);
        } else {
            dataPoint.setComputed(computed.getScore());
        }
        dataPoint.setTarget(goal.getScore());
        dataPoint.setMessageGlobalId(goal.getMessageGlobalId());
        dataPoint.setPublicationDate(context.getMessage().getPublicationDate());
        dataPoint.setUserGlobalId(goal.getUserGlobalId());
        if (computed != null) {
            dataPoint.setBasedOnAdaptedTerms(computed.isBasedOnAdaptedTerms());
        }
        dataPoint.setFeatures(features);

        String out = dataPoint.toParseableString();

        try {
            writer.append(out + "\n");
        } catch (IOException e) {
            throw new EvaluationException(e);
        }
        // EVAL_LOGGER.info(out);
    }

    public int getBasedOnAdaptedTermsCount() {
        return basedOnAdaptedTermsCount;
    }

    public int getCount() {
        return count;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public float getError() {
        return error;
    }

    public Persistence getPersistence() {
        return persistence;
    }

    public double getRmse() {
        return rmse;
    }

    public void resetCurrentCount() {
        currentCount = 0;
    }

    public void setPersistence(Persistence persistence) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null!");
        }
        this.persistence = persistence;
    }

    @Override
    public String toString() {
        return "Evaluator [count=" + count + ", unable=" + unable + ", error=" + error
                + ", sqrError=" + sqrError + ", meanError=" + meanError + ", writer=" + writer
                + ", rmse=" + rmse + ", currentCount=" + currentCount
                + ", basedOnAdaptedTermsCount=" + basedOnAdaptedTermsCount + "]";
    }

}
