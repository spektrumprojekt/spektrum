package de.spektrumprojekt.i.evaluation.runner.processor;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;

public interface EvaluationProcessor extends ConfigurationDescriptable {

    void afterLearning(Observation observation) throws EvaluationException;

    void afterMessage(Message spektrumMessage, MessageFeatureContext context)
            throws EvaluationException;

    void afterRankingMessage(Message spektrumMessage, MessageFeatureContext context)
            throws EvaluationException;

    void afterRun() throws EvaluationException;

    void afterSingleRun() throws EvaluationException;

    /**
     * 
     * @param testRating
     * @param context
     *            null if is not available, so no context for the rating (so probably the computed
     *            rating is 0)
     * @throws EvaluationException
     */
    void afterTest(SpektrumRating testRating, MessageFeatureContext context)
            throws EvaluationException;

    void beforeLearning(Observation observation) throws EvaluationException;

    void beforeMessage(Message spektrumMessage) throws EvaluationException;

    void beforeRun() throws EvaluationException;

    void beforeSingleRun() throws EvaluationException;

    /**
     * 
     * @param testRating
     * @param context
     *            null if is not available, so no context for the rating (so probably the computed
     *            rating is 0)
     * @throws EvaluationException
     */
    void beforeTest(SpektrumRating testRating, MessageFeatureContext context)
            throws EvaluationException;

    int getOrder();

    void onNewDay() throws EvaluationException;

    void onNewMonth() throws EvaluationException;

    void onNewWeek() throws EvaluationException;

    void setup() throws EvaluationException;

}
