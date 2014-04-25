package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TermHistory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TermHistory.class);

    private List<Date> startTimes = new LinkedList<Date>();

    private final Map<String, TermRecord> records = new HashMap<String, TermRecord>();

    public TermHistory(List<Date> startTimes) {
        this.startTimes = startTimes;
    }

    public TermHistory(String serializedHistory) {
        String[] lines = serializedHistory.split(System.getProperty("line.separator"));
        lines[0].split("\\" + ShortTermAnalysisHelper.CSV_SEPARATOR);
        for (String line : lines) {
            String[] data = line.split("\\" + ShortTermAnalysisHelper.CSV_SEPARATOR);
            if (line.startsWith("#")) {
                for (int i = 2; i < data.length; i++) {
                    try {
                        startTimes.add(TermRecord.DATE_FORMAT.parse(data[i]));
                    } catch (ParseException e) {
                        LOGGER.error("Exception during deserialization:", e);
                    }
                }
            }
            if (isRecordOfThisHistory(data[0])) {
                put(new TermRecord(startTimes.size(), line));
            }
        }
    }

    public boolean contains(String term) {
        return records.containsKey(term);
    }

    public TermRecord get(String term) {
        return records.get(term);
    }

    public abstract String getName();

    public Collection<TermRecord> getRecords() {
        return records.values();
    }

    public List<Date> getStartTimes() {
        return startTimes;
    }

    protected abstract boolean isRecordOfThisHistory(String string);

    public TermRecord put(TermRecord record) {
        record.setHistory(this);
        return records.put(record.getTerm(), record);
    }

    public void setStartTimes(List<Date> startTimes) {
        this.startTimes = startTimes;
    }

    public String toParseableString() {
        String lineSeparator = System.getProperty("line.separator");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(TermRecord.getColumnHeaders(startTimes));
        for (TermRecord record : getRecords()) {
            stringBuilder.append(lineSeparator);
            record.toParseableString(stringBuilder);
        }
        return stringBuilder.toString();
    }

}