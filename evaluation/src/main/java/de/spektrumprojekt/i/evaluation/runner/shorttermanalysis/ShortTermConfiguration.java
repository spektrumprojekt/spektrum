package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.io.File;
import java.util.Date;

import de.spektrumprojekt.persistence.Persistence;

public class ShortTermConfiguration {

    private Persistence persistence;

    private String[] useOnlyMessageGroups;

    private long binSize;

    private Date startDate;

    private Date endDate;

    private String folderPath;

    private EnergyAlgorithm[] shortTermAlgorithms;

    private int[] historyLength;

    private int topCount;

    private TermProvider[] termProviders;

    public long getBinSize() {
        return binSize;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public String getHistoriesFilePath() {
        return getWorkingDataFolderPath() + File.separator + "energies.history";
    }

    public int[] getHistoryLength() {
        return historyLength;
    }

    public String getNutritionHistoryFilePath() {
        return getNutritionHistoryFilePath("");
    }

    public String getNutritionHistoryFilePath(String appendix) {
        return getWorkingDataFolderPath() + File.separator + "nutrition" + appendix + ".history";
    }

    public Persistence getPersistence() {
        return persistence;
    }

    public EnergyAlgorithm[] getShortTermAlgorithms() {
        return shortTermAlgorithms;
    }

    public Date getStartDate() {
        return startDate;
    }

    public TermProvider[] getTermProviders() {
        return termProviders;
    }

    public int getTopCount() {
        return topCount;
    }

    public String[] getUseOnlyMessageGroups() {
        return useOnlyMessageGroups;
    }

    public String getWorkingDataFolderPath() {
        return folderPath + File.separator + "data";
    }

    public void setBinSize(long binSize) {
        this.binSize = binSize;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public void setHistoryLength(int[] historyLength) {
        this.historyLength = historyLength;
    }

    public void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }

    public void setShortTermAlgorithms(EnergyAlgorithm[] shortTermAlgorithms) {
        this.shortTermAlgorithms = shortTermAlgorithms;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setTermProviders(TermProvider[] termProviders) {
        this.termProviders = termProviders;
    }

    public void setTopCount(int topCount) {
        this.topCount = topCount;
    }

    public void setUseOnlyMessageGroups(String[] useOnlyMessageGroups) {
        this.useOnlyMessageGroups = useOnlyMessageGroups;
    }
}
