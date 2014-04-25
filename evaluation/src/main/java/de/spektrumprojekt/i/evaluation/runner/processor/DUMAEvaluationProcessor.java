package de.spektrumprojekt.i.evaluation.runner.processor;

import java.io.IOException;

import org.apache.commons.lang3.time.DateUtils;

import de.spektrumprojekt.commons.computer.ComputerAggregation;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;
import de.spektrumprojekt.i.evaluation.runner.umaanlysis.UserModelAdapatationCompareEvaluationElement;
import de.spektrumprojekt.i.evaluation.runner.umaanlysis.UserModelAdapatationCompareEvaluationOutput;
import de.spektrumprojekt.i.evaluation.runner.umaanlysis.UserModelAdaptationReRankEvaluationEventListener;
import de.spektrumprojekt.i.learner.adaptation.DirectedUserModelAdapter;
import de.spektrumprojekt.i.learner.adaptation.UserModelAdaptationReScoreEvent;
import de.spektrumprojekt.i.learner.adaptation.UserModelAdapterConfiguration;
import de.spektrumprojekt.i.scorer.MessageFeatureContext;
import de.spektrumprojekt.i.scorer.ScorerConfiguration;
import de.spektrumprojekt.i.similarity.messagegroup.TermBasedMessageGroupSimilarityComputer;
import de.spektrumprojekt.i.similarity.user.UserSimilarityComputer;
import de.spektrumprojekt.i.similarity.user.UserSimilarityRetriever;
import de.spektrumprojekt.i.similarity.user.UserToUserInterestSelector;
import de.spektrumprojekt.i.similarity.user.hits.HITSUserMentionComputer;
import de.spektrumprojekt.i.similarity.user.hits.HITSUserSelector;
import de.spektrumprojekt.persistence.Persistence;

public class DUMAEvaluationProcessor extends SimpleEvaluationProcessor {

    private final ScorerConfiguration scorerConfiguration;

    private UserModelAdaptationReRankEvaluationEventListener userModelAdaptationReRankEvaluationEventListener;

    private UserModelAdapatationCompareEvaluationOutput userModelAdapatationCompareEvaluationOutput;

    private DirectedUserModelAdapter directUMAdapter;

    public DUMAEvaluationProcessor(EvaluationExecutionProvider evaluationProvider) {
        super(evaluationProvider, 900);

        scorerConfiguration = evaluationProvider.getEvaluationExecuterConfiguration()
                .getScorerConfiguration();
    }

    private void addElementToUserModelAdapatationAnalysis(SpektrumRating testRating,
            UserModelAdaptationReScoreEvent userModelAdaptationReRankEvent) {
        if (userModelAdaptationReRankEvent != null
                && userModelAdapatationCompareEvaluationOutput != null) {

            if (!userModelAdaptationReRankEvent.getAdaptationMessage()
                    .getMessageId().equals(testRating.getMessage().getGlobalId())) {
                throw new RuntimeException(
                        "The message of the adaptation event must be the same as the one of the testrating! wtf!");
            }

            if (!userModelAdaptationReRankEvent.getAdaptationMessage()
                    .getUserGlobalId().equals(testRating.getUserGlobalId())) {
                throw new RuntimeException(
                        "The user of the adaptation event must be the same as the one of the testrating! wtf!");
            }

            UserModelAdapatationCompareEvaluationElement element = new UserModelAdapatationCompareEvaluationElement();
            element.computedAdaptedRank = (double) userModelAdaptationReRankEvent
                    .getMessageFeatureContextOfReScore()
                    .getUserContext(
                            userModelAdaptationReRankEvent.getAdaptationMessage().getUserGlobalId())
                    .getMessageScore().getScore();
            element.computedRankBefore = (double) userModelAdaptationReRankEvent
                    .getAdaptationMessage().getRankBeforeAdaptation().getScore();
            element.targetRank = (double) testRating.getInterest().getScore();
            element.messageId = userModelAdaptationReRankEvent.getAdaptationMessage()
                    .getMessageId();
            element.userId = userModelAdaptationReRankEvent.getAdaptationMessage()
                    .getUserGlobalId();

            userModelAdapatationCompareEvaluationOutput.getElements().add(element);
        }
    }

    @Override
    public void afterMessage(Message spektrumMessage, MessageFeatureContext context)
            throws EvaluationException {
        this.userModelAdaptationReRankEvaluationEventListener.clearEvents();
    }

    @Override
    public void afterSingleRun() throws EvaluationException {

        try {
            userModelAdapatationCompareEvaluationOutput
                    .write(getUserModelAdapatationAnalysisOutputFilename());
        } catch (IOException e) {
            throw new EvaluationException(e);
        }

        getEvaluationExecutionProvider().addConfig(
                "DirectUserModelAdapter Details: " + this.directUMAdapter.getDebugInformation());
        getEvaluationExecutionProvider().addConfig(
                "adapter.adaptedCount: " + directUMAdapter.getAdaptedCount());
        getEvaluationExecutionProvider().addConfig(
                "adapter.requestedAdaptedCount: " + directUMAdapter.getRequestAdaptedCount());

    }

    @Override
    public void beforeTest(SpektrumRating testRating, MessageFeatureContext context)
            throws EvaluationException {
        addElementToUserModelAdapatationAnalysis(
                testRating,
                this.userModelAdaptationReRankEvaluationEventListener.getEventForUser(testRating
                        .getUserGlobalId()));
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    private String getUserModelAdapatationAnalysisOutputFilename() {
        return this.getEvaluationExecutionProvider().getEvaluatorResultFilename() + ".duma";
    }

    @Override
    public void setup() throws EvaluationException {

        Persistence persistence = getEvaluationExecutionProvider().getPersistence();

        UserToUserInterestSelector userSelector = null;
        UserModelAdapterConfiguration umaConf = scorerConfiguration
                .getUserModelAdapterConfiguration();
        if (!umaConf.isValid()) {
            throw new IllegalStateException("UserModelAdapterConfiguration is not valid!. "
                    + umaConf);
        }

        // TODO do registration on more central part (e.g. scorer)

        UserSimilarityComputer userSimilarityComputer = this.getEvaluationExecutionProvider()
                .getScorer().getUserSimilarityComputer();

        int intervallOfUserSimComputationInDays = umaConf.getIntervallOfUserSimComputationInDays();
        if (scorerConfiguration.getUserModelAdapterConfiguration()
                .isUserSelectorUseMentionsPercentage()) {
            userSelector = new UserSimilarityRetriever(persistence,
                    umaConf.getUserSimilarityThreshold());

            this.getEvaluationExecutionProvider()
                    .getTaskRunner()
                    .register(userSimilarityComputer,
                            intervallOfUserSimComputationInDays * DateUtils.MILLIS_PER_DAY,
                            intervallOfUserSimComputationInDays * DateUtils.MILLIS_PER_DAY, false);
        } else if (scorerConfiguration.getUserModelAdapterConfiguration().isUserSelectorUseHITS()) {

            HITSUserMentionComputer hitsUserMentionComputer = new HITSUserMentionComputer(
                    persistence, umaConf.getHitsScoreToUse());

            ComputerAggregation computerAggregation = new ComputerAggregation(
                    userSimilarityComputer, hitsUserMentionComputer);
            userSelector = new HITSUserSelector(hitsUserMentionComputer);

            this.getEvaluationExecutionProvider()
                    .getTaskRunner()
                    .register(computerAggregation,
                            intervallOfUserSimComputationInDays * DateUtils.MILLIS_PER_DAY,
                            intervallOfUserSimComputationInDays * DateUtils.MILLIS_PER_DAY, false);
        }

        TermBasedMessageGroupSimilarityComputer messageGroupSimilarityRetriever;
        if (this.scorerConfiguration.getUserModelAdapterConfiguration()
                .isAdaptFromMessageGroups()) {
            int intervallInDays = umaConf.getMessageGroupSimilarityConfiguration()
                    .getIntervallOfMGSimComputationInDays();
            messageGroupSimilarityRetriever = new TermBasedMessageGroupSimilarityComputer(
                    persistence,
                    this.scorerConfiguration.getUserModelAdapterConfiguration()
                            .getMessageGroupSimilarityConfiguration());

            this.getEvaluationExecutionProvider()
                    .getTaskRunner()
                    .register(messageGroupSimilarityRetriever,
                            intervallInDays * DateUtils.MILLIS_PER_DAY,
                            intervallInDays * DateUtils.MILLIS_PER_DAY, false);
        } else {
            messageGroupSimilarityRetriever = null;
            if (userSelector == null && !umaConf.isAdaptFromMessageGroups()) {
                throw new IllegalStateException(
                        "UserModelAdapterConfiguration is not valid!. userSelector is still null."
                                + umaConf);
            }

        }

        if (!scorerConfiguration.getUserModelConfigurations().containsKey(
                UserModel.DEFAULT_USER_MODEL_TYPE)) {
            throw new RuntimeException("UserModelType " + UserModel.DEFAULT_USER_MODEL_TYPE
                    + " is not included in rankerConfiguration but needed for DUMA.");
        }

        directUMAdapter = new DirectedUserModelAdapter(
                persistence,
                this.getEvaluationExecutionProvider().getScorer(),
                UserModel.DEFAULT_USER_MODEL_TYPE,
                userSelector,
                messageGroupSimilarityRetriever,
                this.scorerConfiguration.getUserModelAdapterConfiguration(),
                true);

        this.userModelAdaptationReRankEvaluationEventListener = new UserModelAdaptationReRankEvaluationEventListener();
        this.userModelAdapatationCompareEvaluationOutput = new UserModelAdapatationCompareEvaluationOutput();
        directUMAdapter.getUserModelAdaptationReScoreEventHandler().addEventListener(
                this.userModelAdaptationReRankEvaluationEventListener);

        this.getEvaluationExecutionProvider().getCommunicator()
                .registerMessageHandler(directUMAdapter);

    }

}
