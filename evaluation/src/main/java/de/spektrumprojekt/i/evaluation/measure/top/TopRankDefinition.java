package de.spektrumprojekt.i.evaluation.measure.top;

public class TopRankDefinition {
    private int calendarField;
    private int topN;

    public TopRankDefinition(int topN, int calendarField) {
        this.topN = topN;
        this.calendarField = calendarField;
    }

    public int getCalendarField() {
        return calendarField;
    }

    public String getEvalsFilename(String basicFilename) {
        return getRanksFilename(basicFilename) + ".eval";
    }

    public String getRanksFilename(String basicFilename) {
        return basicFilename + "-top" + topN + TopRankMessageComputer.getNameForCalendarField(calendarField)
                + ".ranks";
    }

    public int getTopN() {
        return topN;
    }

    @Override
    public String toString() {
        return "TopRankDefinition [calendarField=" + calendarField + ", topN=" + topN + "]";
    }
}