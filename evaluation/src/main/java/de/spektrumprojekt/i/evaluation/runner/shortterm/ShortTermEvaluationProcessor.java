package de.spektrumprojekt.i.evaluation.runner.shortterm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.i.evaluation.runner.processor.EvaluationException;
import de.spektrumprojekt.i.evaluation.runner.processor.EvaluationExecutionProvider;
import de.spektrumprojekt.i.evaluation.runner.processor.SimpleEvaluationProcessor;
import de.spektrumprojekt.i.learner.time.TimeBinnedUserModelEntryIntegrationStrategy;
import de.spektrumprojekt.i.scorer.ScorerConfiguration;
import de.spektrumprojekt.i.timebased.ShortTermUserModelUpdater;
import de.spektrumprojekt.i.timebased.SimpleLongTermUserModelUpdater;
import de.spektrumprojekt.i.timebased.UserModelUpdater;
import de.spektrumprojekt.i.timebased.config.ShortTermMemoryConfiguration;

public class ShortTermEvaluationProcessor extends SimpleEvaluationProcessor {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(ShortTermEvaluationProcessor.class);

    private final static int ORDER = 2000;

    private ShortTermMemoryConfiguration shortTermMemoryConfiguration;
    private UserModelUpdater modelUpdater;

    public ShortTermEvaluationProcessor(EvaluationExecutionProvider evaluationProvider) {
        super(evaluationProvider, ORDER);
    }

    @Override
    public void beforeSingleRun() throws EvaluationException {

        ScorerConfiguration scorerConfiguration = this.getEvaluationExecutionProvider()
                .getEvaluationExecuterConfiguration().getScorerConfiguration();
        shortTermMemoryConfiguration = this.getEvaluationExecutionProvider()
                .getEvaluationExecuterConfiguration().getScorerConfiguration()
                .getShortTermMemoryConfiguration();
        if (shortTermMemoryConfiguration != null) {

            if (shortTermMemoryConfiguration.isUseSimpleLongTermUpdater()) {

                final TimeBinnedUserModelEntryIntegrationStrategy shortTermEntryIntegrationStrategy = (TimeBinnedUserModelEntryIntegrationStrategy) this
                        .getEvaluationExecutionProvider().getLearner()
                        .getUserModelEntryIntegrationStrategies()
                        .get(UserModel.SHORT_TERM_USER_MODEL_TYPE);

                modelUpdater = new SimpleLongTermUserModelUpdater(this
                        .getEvaluationExecutionProvider()
                        .getPersistence(), scorerConfiguration,
                        shortTermEntryIntegrationStrategy);

            } else if (shortTermMemoryConfiguration.getEnergyCalculationConfiguration() != null) {

                modelUpdater = new ShortTermUserModelUpdater(this.getEvaluationExecutionProvider()
                        .getPersistence(), scorerConfiguration);
            }
            LOGGER.info("Using userModelUpdater: " + modelUpdater);
        }
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void onNewDay() throws EvaluationException {

        if (modelUpdater != null) {

            modelUpdater.updateUserModels();
        }
    }

}
