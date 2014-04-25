package de.spektrumprojekt.i.evaluation.runner.timetracker;

import de.spektrumprojekt.commons.output.SpektrumParseableElementFileOutput;

public class BinTermStatsOutput extends SpektrumParseableElementFileOutput<BinTermStats> {

    public BinTermStatsOutput() {
        super(BinTermStats.class);
    }

    @Override
    protected BinTermStats createNewElement(String line) {
        return new BinTermStats(line);
    }

    @Override
    protected String getHeader() {
        return BinTermStats.getColumnHeaders();
    }

}