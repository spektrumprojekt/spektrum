package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TermRecord {

    private static final Logger LOGGER = LoggerFactory.getLogger(TermRecord.class);

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public static String getColumnHeaders(List<Date> startTimes) {
        LinkedList<String> columnHeaders = new LinkedList<String>();
        columnHeaders.addLast("#historyType");
        columnHeaders.addLast("term");
        for (Date date : startTimes) {
            columnHeaders.addLast(DATE_FORMAT.format(date));
        }
        return StringUtils.join(columnHeaders.toArray(new String[] { }),
                ShortTermAnalysisHelper.CSV_SEPARATOR);
    }

    // for deserialization
    private TermHistory history;

    private final String term;

    private Double[] entries;

    public TermRecord(int expectedLength, String line) {
        String[] data = ShortTermAnalysisHelper.splitLine(expectedLength, line);
        term = data[1];
        entries = new Double[data.length - 2];
        for (int i = 2; i < data.length; i++) {
            entries[i - 2] = Double.parseDouble(data[i]);
        }
    }

    public TermRecord(String term, int entryCount) {
        entries = new Double[entryCount];
        for (int i = 0; i < entryCount; i++) {
            entries[i] = 0d;
        }
        this.term = term;
    }

    public Double[] getEntries() {
        return entries;

    }

    public double getEntry(int index) {
        return entries[index];
    }

    public String getTerm() {
        return term;
    }

    public void setEntries(Double[] entries) {
        this.entries = entries;
    }

    public void setEntry(int index, double value) {
        entries[index] = value;
    }

    public void setHistory(TermHistory history) {
        this.history = history;
    }

    public void toParseableString(StringBuilder stringBuilder) {
        stringBuilder.append(history.getName());
        stringBuilder.append(ShortTermAnalysisHelper.CSV_SEPARATOR);
        stringBuilder.append(term);
        for (Double entry : entries) {
            stringBuilder.append(ShortTermAnalysisHelper.CSV_SEPARATOR);
            stringBuilder.append(String.valueOf(entry));
        }
    }
}
