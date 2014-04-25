package de.spektrumprojekt.i.evaluation.runner.processor;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;
import de.spektrumprojekt.i.scorer.MessageFeatureContext;

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

    @Override
    public void afterLearning(Observation observation) throws EvaluationException {
    }

    public void afterMessage(Message spektrumMessage, MessageFeatureContext context)
            throws EvaluationException {

    }

    public void afterScoringMessage(Message spektrumMessage, MessageFeatureContext context)
            throws EvaluationException {
    }

    @Override
    public void afterRun() throws EvaluationException {
    }

    @Override
    public void afterSingleRun() throws EvaluationException {
    }

    public void afterTest(SpektrumRating testRating, MessageFeatureContext context)
            throws EvaluationException {
    }

    public void afterTraining() throws EvaluationException {
    }

    @Override
    public void beforeLearning(Observation observation) throws EvaluationException {
    }

    @Override
    public void beforeMessage(Message spektrumMessage) throws EvaluationException {
    }

    @Override
    public void beforeRun() throws EvaluationException {
    }

    @Override
    public void beforeSingleRun() throws EvaluationException {
    }

    public void beforeTest(SpektrumRating testRating, MessageFeatureContext context)
            throws EvaluationException {
    }

    public void beforeTraining() throws EvaluationException {
    }

    @Override
    public abstract String getConfigurationDescription();

    public EvaluationExecutionProvider getEvaluationExecutionProvider() {
        return evaluationExecutionProvider;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void onNewDay() throws EvaluationException {
    }

    @Override
    public void onNewMonth() throws EvaluationException {
    }

    @Override
    public void onNewWeek() throws EvaluationException {
    }

    @Override
    public void setup() throws EvaluationException {

    }
}
