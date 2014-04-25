package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.io.File;

public abstract class ResultFilter {

    public static ResultFilter[] getFilters() {
        return new ResultFilter[] {};
    }

    // filepath;
    ShortTermConfiguration configuration;

    public ResultFilter(ShortTermConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * data to analyze
     * 
     * @param history
     *            result
     * @return
     */
    public abstract TermHistory analyse();

    protected abstract String getFileName();

    public String getFilePath() {
        return configuration.getWorkingDataFolderPath() + File.separator + getFileName();
    }
}
