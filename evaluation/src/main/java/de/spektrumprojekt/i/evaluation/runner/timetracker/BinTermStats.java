package de.spektrumprojekt.i.evaluation.runner.timetracker;

import de.spektrumprojekt.commons.output.SpektrumParseableElement;

public class BinTermStats implements SpektrumParseableElement {

    public static String getColumnHeaders() {
        return "messageCount " + "termCount " + "uniqueTermCount " + "newTermCount "
                + "removedTermCount "
                + "matchingTermCount " + "reallyNewTermCount " + "matchingAllTermCount";
    }

    public int messageCount;
    public int termCount;
    public int uniqueTermCount;
    public int newTermCount;
    public int removedTermCount;
    public int matchingTermCount;
    public int reallyNewTermCount;

    public int matchingAllTermCount;

    public BinTermStats() {

    }

    public BinTermStats(String line) {
        String[] vals = line.split(line);
        int index = 0;
        messageCount = Integer.parseInt(vals[index++]);
        termCount = Integer.parseInt(vals[index++]);
        uniqueTermCount = Integer.parseInt(vals[index++]);
        newTermCount = Integer.parseInt(vals[index++]);
        removedTermCount = Integer.parseInt(vals[index++]);
        matchingTermCount = Integer.parseInt(vals[index++]);
        reallyNewTermCount = Integer.parseInt(vals[index++]);
        matchingAllTermCount = Integer.parseInt(vals[index++]);

    }

    public String toParseableString() {
        return toString();
    }

    @Override
    public String toString() {
        return messageCount + " " + termCount + " " + uniqueTermCount + " " + newTermCount
                + " " + removedTermCount
                + " " + matchingTermCount + " " + reallyNewTermCount + " "
                + matchingAllTermCount;
    }
}