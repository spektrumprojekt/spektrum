package de.spektrumprojekt.i.evaluation.runner.configuration;

import java.util.Date;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.i.evaluation.MessageDataSetProvider;
import de.spektrumprojekt.i.scorer.ScorerConfiguration;
import de.spektrumprojekt.i.scorer.UserSpecificMessageFeatureContext;

public interface EvaluationExecuterConfiguration {

    public String getComment();

    public MessageDataSetProvider getDataSetProvider();

    public EvaluatorConfiguration getEvaluatorConfiguration();

    public float getLetTheComputedBePositive();

    public float getLetTheGoalBePositive();

    public Date getMessageAnalyzerOnlyAnalyzeAfter();

    public String getName();

    public int getPriority();

    public Double getRandomScorerThresholdOfScoringRelevant();

    public ScorerConfiguration getScorerConfiguration();

    public <T extends Command<UserSpecificMessageFeatureContext>> Class<T> getSpecialScoreCommandClass();

    public Date getStartDate();

    public int getUniqueId();

    public boolean isLogTermFrequency();

    public boolean isOutputMessageGroupSimilarity();

    public boolean isOutputTerms();

    public boolean isUseCollabScorer();

    public boolean isUseIterativeCollabScorer();

    public boolean isUseMessageAnalyzer();

    public void recycle();

    public void validate();
}