package de.spektrumprojekt.i.commons.valueaggregator;

import de.spektrumprojekt.i.learner.adaptation.ValueAggregator;

public class VotingValueAggregator implements ValueAggregator {

    private final boolean strict;
    private final double strictTreshold;
    private final int necassaryVotes;

    private int votes;

    public VotingValueAggregator(int necassaryVotes) {
        this(necassaryVotes, false, 0d);
    }

    public VotingValueAggregator(int necassaryVotes, boolean strict, double strictTreshold) {
        if (necassaryVotes <= 0) {
            throw new IllegalArgumentException("necassaryVotes must be greater 0.");
        }
        if (strict && (strictTreshold < 0 || strictTreshold > 1)) {
            throw new IllegalArgumentException(
                    "If strict, strictThreshold must be >= 0 and =< 1 but is " + strictTreshold);
        }
        this.strict = strict;
        this.necassaryVotes = necassaryVotes;
        this.strictTreshold = strictTreshold;
    }

    @Override
    public void add(double value, double countWeight) {
        votes++;
    }

    @Override
    public double getValue() {
        double val = (double) votes / (double) necassaryVotes;
        if (strict) {
            val = val >= strictTreshold ? 1 : 0;
        }
        return Math.max(Math.min(val, 1), 0);
    }

    @Override
    public String toString() {
        return "VotingValueAggregator [strict=" + strict + ", strictTreshold=" + strictTreshold
                + ", necassaryVotes=" + necassaryVotes + ", votes=" + votes + "]";
    }

}