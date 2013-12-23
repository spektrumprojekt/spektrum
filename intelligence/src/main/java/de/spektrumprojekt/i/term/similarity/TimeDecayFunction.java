package de.spektrumprojekt.i.term.similarity;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

public class TimeDecayFunction {

    public static TimeDecayFunction createWithDayCutOff() {
        return new TimeDecayFunction(DateUtils.MILLIS_PER_DAY);
    }

    public static TimeDecayFunction createWithWeekCutOff() {
        return new TimeDecayFunction(7 * DateUtils.MILLIS_PER_DAY);
    }

    // time intervall when the decay should be 0.9
    private final long zeroPointNineCutOffInMs;

    private final double alpha;

    public TimeDecayFunction(long zeroPointNineCutOffInMs) {
        if (zeroPointNineCutOffInMs <= 0) {
            throw new IllegalArgumentException("zeroPointNineCutOffInMs ("
                    + zeroPointNineCutOffInMs + ") must be > 0)");
        }
        this.zeroPointNineCutOffInMs = zeroPointNineCutOffInMs;

        alpha = -(Math.log(0.9d) / zeroPointNineCutOffInMs);
    }

    public double getDecay(Date dateForDecay, Date now) {
        return this.getDecay(dateForDecay.getTime(), now.getTime());
    }

    public double getDecay(long intervall) {
        if (intervall < 0) {
            throw new IllegalArgumentException("intervall (" + intervall
                    + ") for decay must be >= 0.");
        }
        double ret = Math.exp(-(alpha * intervall));
        return ret;
    }

    public double getDecay(long dateForDecayInMs, long nowInMs) {
        long intervall = nowInMs - dateForDecayInMs;
        if (intervall < 0) {
            throw new IllegalArgumentException("dateForDecay (" + dateForDecayInMs
                    + ")is < as now ("
                    + nowInMs + ")");
        }
        return getDecay(intervall);
    }

    @Override
    public String toString() {
        return "TimeDecayFunction [zeroPointNineCutOffInMs=" + zeroPointNineCutOffInMs
                + ", alpha=" + alpha + "]";
    }
}