package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.TermProvider.Entry;

public class ShortTermAnalysis {

    private final static Logger LOGGER = LoggerFactory.getLogger(ShortTermAnalysis.class);

    // public static File getShortTermAnalysisResultsFile(String evaluatorResultFilename) {
    // return new File(evaluatorResultFilename + ".shortTermAnalysisResults.csv");
    // }

    // private final Persistence persistence;

    private final ShortTermConfiguration configuration;

    private List<Date> startTimes = new LinkedList<Date>();

    private NutritionHistory nutritionHistory;

    private final List<EnergyHistory> energyHistories = new LinkedList<EnergyHistory>();

    public ShortTermAnalysis(ShortTermConfiguration configuration) {
        super();
        this.configuration = configuration;
        createFolders();
    }

    private void createFolders() {
        new File(configuration.getFolderPath()).mkdirs();
        new File(configuration.getWorkingDataFolderPath()).mkdirs();

    }

    private void createTermEntryIfNecessary(NutritionHistory nutritionHistory, String term) {
        if (!nutritionHistory.contains(term)) {
            nutritionHistory.put(new TermRecord(term, startTimes.size()));
        }
    }

    public void doAnalysis() {
        ShortTermAnalysisHelper.extractBinStarttimes(configuration, startTimes);
        doTermAnalysis();
        ShortTermAnalysisHelper.writeHistory(configuration.getNutritionHistoryFilePath(),
                nutritionHistory);
        doEnergyAnalysis();
        ShortTermAnalysisHelper.writeHistories(configuration.getHistoriesFilePath(),
                nutritionHistory, energyHistories);
    }

    private void doEnergyAnalysis() {
        initializeAlgorithms();
        for (TermRecord record : nutritionHistory.getRecords()) {
            for (EnergyHistory energyHistory : energyHistories) {
                energyHistory.calculateEnergy(record);
            }
        }
    }

    public void doEnergyAnalysisOnly() {
        loadNutritionHistory();
        doEnergyAnalysis();
        ShortTermAnalysisHelper.writeHistories(configuration.getHistoriesFilePath(),
                nutritionHistory, energyHistories);
        filterResults();
    }

    private void doTermAnalysis() {
        nutritionHistory = new NutritionHistory(startTimes);
        NutritionHistory nutritionHistory = this.nutritionHistory;
        TermProvider[] termProviders = configuration.getTermProviders();

        for (TermProvider termProvider : termProviders) {
            // only the first nutitionhistory is used to calculate energies and scores, exchange the
            // files and run doEnergyAnalysisOnly() for other histories
            if (termProviders[0] != termProvider) {
                nutritionHistory = new NutritionHistory(startTimes);
            }

            entryLoop: for (Entry entry : termProvider.getTerms()) {
                String termValue = entry.getTerm();
                if (termValue.contains(ShortTermAnalysisHelper.CSV_SEPARATOR)) {
                    LOGGER.debug("skipped term {}", entry);
                    continue entryLoop;
                }
                if (!messageGroupSupported(termValue)) {
                    continue entryLoop;
                }
                createTermEntryIfNecessary(nutritionHistory, termValue);
                int index = ShortTermAnalysisHelper.getIndex(startTimes, entry.getDate());
                TermRecord record = nutritionHistory.get(termValue);
                record.setEntry(index, record.getEntry(index) + entry.getScore());

            }
            ShortTermAnalysisHelper.writeHistory(
                    configuration.getNutritionHistoryFilePath(termProvider.getFileAppendix()),
                    nutritionHistory);

        }
    }

    private void filterResults() {
        List<ResultFilter> filters = new LinkedList<ResultFilter>();
        filters.add(new MaxTopFilter(nutritionHistory, configuration));
        filters.add(new GainTopFilter(nutritionHistory, configuration));
        filters.add(new SumTopFilter(nutritionHistory, configuration));
        for (EnergyHistory history : energyHistories) {
            filters.add(new MaxTopFilter(history, configuration));
        }
        for (ResultFilter filter : filters) {
            ShortTermAnalysisHelper.writeHistory(filter.getFilePath(), filter.analyse());
        }
    }

    private void initializeAlgorithms() {
        energyHistories.clear();
        for (EnergyAlgorithm algorithm : EnergyAlgorithm.getAlgorithms()) {
            for (int historyLength : configuration.getHistoryLength()) {
                energyHistories.add(new EnergyHistory(startTimes, historyLength, algorithm));
            }
        }
    }

    private void loadNutritionHistory() {
        try {
            nutritionHistory = ShortTermAnalysisHelper.readNutritionHistory(configuration
                    .getNutritionHistoryFilePath());
            startTimes = nutritionHistory.getStartTimes();
        } catch (IOException e) {
            LOGGER.error("Could not load nutition.history", e);
            throw new RuntimeException(e);
        }
    }

    private boolean messageGroupSupported(String termValue) {
        if (configuration.getUseOnlyMessageGroups() == null
                || configuration.getUseOnlyMessageGroups().length == 0) {
            return true;
        }
        for (String messageGroup : configuration.getUseOnlyMessageGroups()) {
            if (termValue.startsWith(messageGroup + "#")) {
                return true;
            }
        }
        return false;
    }
}