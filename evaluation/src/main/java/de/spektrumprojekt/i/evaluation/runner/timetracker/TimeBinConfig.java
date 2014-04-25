package de.spektrumprojekt.i.evaluation.runner.timetracker;

public class TimeBinConfig {

    private final TimeBinMode timeBinLength;

    private final TimeBinMode timeBinIncrement;

    public TimeBinConfig(TimeBinMode length, TimeBinMode increment) {

        this.timeBinLength = length;
        this.timeBinIncrement = increment;

    }

    public TimeBinMode getTimeBinIncrement() {
        return timeBinIncrement;
    }

    public TimeBinMode getTimeBinLength() {
        return timeBinLength;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TimeBinConfig [timeBinLength=");
        builder.append(timeBinLength);
        builder.append(", timeBinIncrement=");
        builder.append(timeBinIncrement);
        builder.append("]");
        return builder.toString();
    }
}