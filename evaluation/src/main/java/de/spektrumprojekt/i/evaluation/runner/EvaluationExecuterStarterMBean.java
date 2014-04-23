package de.spektrumprojekt.i.evaluation.runner;

import java.util.Date;

import de.spektrumprojekt.i.evaluation.runner.configuration.EvaluationExecuterConfiguration;

public interface EvaluationExecuterStarterMBean {

    public long getAverageTimePerProcessedConfiguration();

    public EvaluationExecuterConfiguration getCurrentConfiguration();

    public String getCurrentConfigurationName();

    public double getCurrentNumberOfProcessedMessagesRatio();

    public int getFilteredConfigurationsSize();

    public int getNumberOfConfigurationsProcessed();

    public double getRatioOfProcessedConfiguarations();

    public Date getRunningStartTime();

    public long getTimeRunSoFar();

    public boolean isEndGracefully();

    public boolean isRunMeasureComputer();

    public boolean isRunTopRankMessageComputer();

    public void revokeStopOnNextConfguration();

    public void stopOnNextConfiguration();

}