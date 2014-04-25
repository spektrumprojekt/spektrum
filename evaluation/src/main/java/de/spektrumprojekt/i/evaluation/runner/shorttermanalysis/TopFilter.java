package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public abstract class TopFilter extends ResultFilter {
    private class Entry {
        private final double score;
        private final TermRecord record;

        public Entry(double score, TermRecord record) {
            super();
            this.score = score;
            this.record = record;
        }

        public TermRecord getRecord() {
            return record;
        }

        public double getScore() {
            return score;
        }
    }

    TermHistory history;
    List<Entry> entries = new LinkedList<TopFilter.Entry>();

    public TopFilter(TermHistory history, ShortTermConfiguration configuration) {
        super(configuration);
        this.history = history;
    }

    @Override
    public TermHistory analyse() {
        for (TermRecord record : history.getRecords()) {
            double score = calculateScore(record);
            entries.add(new Entry(score, record));
        }
        Collections.sort(entries, new Comparator<Entry>() {
            @Override
            public int compare(Entry o1, Entry o2) {
                if (o1.getScore() > o2.getScore()) {
                    return -1;
                } else if (o2.getScore() > o1.getScore()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        final LinkedList<TermRecord> results = new LinkedList<TermRecord>();
        for (int i = 0; i < configuration.getTopCount(); i++) {
            results.addLast(entries.get(i).getRecord());
        }
        TermHistory result = new TermHistory(history.getStartTimes()) {
            @Override
            public String getName() {
                return history.getName();
            }

            @Override
            public Collection<TermRecord> getRecords() {
                return results;
            }

            @Override
            protected boolean isRecordOfThisHistory(String string) {
                return history.isRecordOfThisHistory(string);

            }
        };
        return result;
    }

    public abstract double calculateScore(TermRecord record);

    @Override
    protected String getFileName() {
        return history.getName() + "_" + getFilterMethodName() + "_top_"
                + configuration.getTopCount() + ".history";
    }

    protected abstract String getFilterMethodName();

}
