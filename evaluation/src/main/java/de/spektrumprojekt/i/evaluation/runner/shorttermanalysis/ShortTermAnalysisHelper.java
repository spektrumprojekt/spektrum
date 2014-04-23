package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShortTermAnalysisHelper {

    public static final String CSV_SEPARATOR = "^";

    private static final Logger LOGGER = LoggerFactory.getLogger(ShortTermAnalysisHelper.class);

    public static int calculateExpectedRecordNumber(List<Date> startTimes) {
        return startTimes.size() + 2;
    }

    public static void extractBinStarttimes(ShortTermConfiguration configuration,
            List<Date> startTimes) {
        if (configuration.getStartDate() == null || configuration.getEndDate() == null) {
            throw new IllegalArgumentException("StartDate and EndDate must be set!");
        }
        long startTime = configuration.getStartDate().getTime();
        long currentTime = startTime;
        while (currentTime < configuration.getEndDate().getTime()) {
            startTimes.add(new Date(currentTime));
            currentTime += configuration.getBinSize();
        }
        ShortTermAnalysisHelper.sort(startTimes);
    }

    /**
     * 
     * @param dates
     *            ordered List, oldest date first
     * @param date
     *            dx
     * @return if d1< dx <d2 d1 will be returned
     */
    public static int getIndex(List<Date> dates, Date date) {
        lookupIndex: for (int i = 0; i < dates.size(); i++) {
            if (date.getTime() >= dates.get(i).getTime()) {
                continue lookupIndex;
            }
            return i - 1;
        }
        return dates.size() - 1;
    }

    private static String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        IOUtils.closeQuietly(reader);
        return stringBuilder.toString();
    }

    public static NutritionHistory readNutritionHistory(String path) throws IOException {
        String content = readFile(path);
        return new NutritionHistory(content);
    }

    public static void sort(List<Date> dates) {
        Collections.sort(dates, new Comparator<Date>() {
            public int compare(Date o1, Date o2) {
                return (int) (o1.getTime() - o2.getTime());
            }
        });
    }

    public static String[] splitLine(int expectedLength, String line) {
        // String[] splitted = line.split("\\s+");
        String[] splitted = line.split("\\" + CSV_SEPARATOR);
        // if (!(splitted.length > expectedLength)) {
        return splitted;
        // } else {
        // String[] result = new String[expectedLength];
        // result[0] = splitted[0];
        // int dimension = splitted.length - expectedLength;
        // result[1] = "";
        // for (int i = 1; i < splitted.length; i++) {
        // if (i <= 1 + dimension) {
        // result[1] += " " + splitted[i];
        // } else {
        // result[i - dimension] = splitted[i];
        // }
        // }
        // return result;
        // }
    }

    public static void writeHistories(String path, NutritionHistory nutritionHistory,
            List<EnergyHistory> histories) {
        File file = new File(path);
        FileWriter writer;
        try {
            writer = new FileWriter(file);
            writer.write(nutritionHistory.toParseableString(histories));
            writer.close();
        } catch (IOException e) {
            LOGGER.warn("could not write history", e);
        }
    }

    public static void writeHistory(String path, TermHistory history) {
        File file = new File(path);
        FileWriter writer;
        try {
            writer = new FileWriter(file);
            writer.write(history.toParseableString());
            writer.close();
        } catch (IOException e) {
            LOGGER.warn("could not write history", e);
        }
    }

    private ShortTermAnalysisHelper() {
        // only heper class
    }
}
