package de.spektrumprojekt.i.evaluation.runner.configuration;

import java.util.Date;

import de.spektrumprojekt.i.evaluation.MessageDataSetProviderLoader;
import de.spektrumprojekt.i.evaluation.runner.EvaluationExecuterStarter;

public abstract class EvaluationExecuterConfigurationCreator {

    private final int priorityFactor;

    private final MessageDataSetProviderLoader<?> dataSetProviderClazz;

    private final Date standardDate;

    public EvaluationExecuterConfigurationCreator(
            MessageDataSetProviderLoader<?> dataSetProviderClazz,
            Date standardDate, int priorityFactor) {
        this.dataSetProviderClazz = dataSetProviderClazz;
        this.standardDate = standardDate;
        this.priorityFactor = priorityFactor;
    }

    public abstract void addConfiguration(EvaluationExecuterStarter starter);

    public MessageDataSetProviderLoader<?> getDataSetProviderClazz() {
        return dataSetProviderClazz;
    }

    public int getPriorityFactor() {
        return priorityFactor;
    }

    public Date getStandardDate() {
        return standardDate;
    }

}
