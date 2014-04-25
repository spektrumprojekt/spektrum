package de.spektrumprojekt.i.evaluation.runner.like;

import java.util.Collection;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.observation.Interest;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.datamodel.observation.ObservationPriority;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.evaluation.runner.processor.EvaluationException;
import de.spektrumprojekt.i.evaluation.runner.processor.EvaluationExecutionProvider;
import de.spektrumprojekt.i.evaluation.runner.processor.SimpleEvaluationProcessor;
import de.spektrumprojekt.i.learner.LearningMessage;
import de.spektrumprojekt.i.scorer.MessageFeatureContext;

public class LikeLearnerEvaluationProcessor extends SimpleEvaluationProcessor {

    private final static int ORDER = 2000;

    public LikeLearnerEvaluationProcessor(EvaluationExecutionProvider evaluationProvider) {
        super(evaluationProvider, ORDER);
    }

    @Override
    public void afterScoringMessage(Message spektrumMessage, MessageFeatureContext context)
            throws EvaluationException {

        Collection<String> likeUserGlobalIds = MessageHelper.getUserLikes(spektrumMessage);

        for (String likeUser : likeUserGlobalIds) {
            LearningMessage learningMessage = new LearningMessage(new Observation(
                    likeUser,
                    spektrumMessage.getGlobalId(),
                    ObservationType.LIKE,
                    ObservationPriority.USER_INTERACTION,
                    null,
                    spektrumMessage.getPublicationDate(),
                    Interest.EXTREME)
                    );

            getEvaluationExecutionProvider().getLearner().deliverMessage(learningMessage);
        }

    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

}
