package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.io.File;

import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

public class ShortTermAnalysisStarter {

    private static void doConfiguration(ShortTermConfiguration configuration) {
        configuration.setFolderPath(System.getProperty("user.dir") + File.separator
                + "analysisTest");
        configuration.setHistoryLength(new int[] { 2, 4 });
        configuration.setTopCount(100);
        configuration.setUseOnlyMessageGroups(new String[] { "" });
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Persistence persistence = new SimplePersistence();
        ShortTermConfiguration configuration = new ShortTermConfiguration();
        doConfiguration(configuration);
        configuration.setPersistence(persistence);
        ShortTermAnalysis analysis = new ShortTermAnalysis(configuration);
        analysis.doEnergyAnalysisOnly();

    }
}
