package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.util.Date;
import java.util.List;

public class NutritionHistory extends TermHistory {

    public NutritionHistory(List<Date> startTimes) {
        super(startTimes);
    }

    public NutritionHistory(String serializedHistory) {
        super(serializedHistory);
    }

    @Override
    public String getName() {
        return "nutritionHistory";
    }

    @Override
    protected boolean isRecordOfThisHistory(String string) {
        return string.equals(getName());
    }

    public String toParseableString(List<EnergyHistory> histories) {
        String lineSeparator = System.getProperty("line.separator");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(TermRecord.getColumnHeaders(getStartTimes()));
        for (TermRecord record : getRecords()) {
            stringBuilder.append(lineSeparator);
            record.toParseableString(stringBuilder);
            for (TermHistory history : histories) {
                stringBuilder.append(lineSeparator);
                history.get(record.getTerm()).toParseableString(stringBuilder);

            }
        }
        return stringBuilder.toString();
    }
}
