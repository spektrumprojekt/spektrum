package de.spektrumprojekt.i.evaluation.runner.processor;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;

/**
 * Does not much, just to simplify implementation
 * 
 */
public abstract class SimpleEvaluationProcessor implements EvaluationProcessor {

    private final int order;

    private final EvaluationExecutionProvider evaluationExecutionProvider;

    public SimpleEvaluationProcessor(EvaluationExecutionProvider evaluationProvider, int order) {
        if (evaluationProvider == null) {
            throw new IllegalArgumentException("evaluationProvider cannot be null.");
        }
        this.evaluationExecutionProvider = evaluationProvider;
        this.order = order;
    }

    public void afterLearning(Observation observation) throws EvaluationException {
    }

    public void afterMessage(Message spektrumMessage, MessageFeatureContext context)
            throws EvaluationException {

    }

    public void afterRankingMessage(Message spektrumMessage, MessageFeatureContext context)
            throws EvaluationException {
    }

    public void afterRun() throws EvaluationException {
    }

    public void afterSingleRun() throws EvaluationException {
    }

    public void afterTest(SpektrumRating testRating, MessageFeatureContext context)
            throws EvaluationException {
    }

    public void afterTraining() throws EvaluationException {
    }

    public void beforeLearning(Observation observation) throws EvaluationException {
    }

    public void beforeMessage(Message spektrumMessage) throws EvaluationException {
    }

    public void beforeRun() throws EvaluationException {
    }

    public void beforeSingleRun() throws EvaluationException {
    }

    public void beforeTest(SpektrumRating testRating, MessageFeatureContext context)
            throws EvaluationException {
    }

    public void beforeTraining() throws EvaluationException {
    }

    public abstract String getConfigurationDescription();

    public EvaluationExecutionProvider getEvaluationExecutionProvider() {
        return evaluationExecutionProvider;
    }

    public int getOrder() {
        return order;
    }

    public void onNewDay() throws EvaluationException {
    }

    public void onNewMonth() throws EvaluationException {
    }

    public void onNewWeek() throws EvaluationException {
    }

    public void setup() throws EvaluationException {

    }
}
