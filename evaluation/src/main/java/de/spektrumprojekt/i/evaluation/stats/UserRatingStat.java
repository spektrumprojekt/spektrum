package de.spektrumprojekt.i.evaluation.stats;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.spektrumprojekt.datamodel.message.InteractionLevel;
import de.spektrumprojekt.datamodel.observation.Interest;

public class UserRatingStat {

    public static String toHeaderString(long minMonth, long maxMonth) {
        StringBuilder sb = new StringBuilder();

        IntegerWrapper headerCount = new IntegerWrapper();
        headerCount.value = 1;
        sb.append(StringUtils.join(
                new String[] { headerCount.value++ + "_userId",
                        RatingCount.toHeaderString("all", headerCount) }, " "));

        for (long i = minMonth; i <= maxMonth; i++) {
            sb.append(" "
                    + RatingCount.toHeaderString("month_" + (i - minMonth), headerCount));
        }
        return sb.toString();
    }

    public String userId;

    public final RatingCount ratingCount = new RatingCount();

    public Map<Long, UserTimeRatingStat> monthStats = new HashMap<Long, UserTimeRatingStat>();

    private UserTimeRatingStat get(Map<Long, UserTimeRatingStat> stats, long field) {
        UserTimeRatingStat stat = stats.get(field);
        if (stat == null) {
            stat = new UserTimeRatingStat();
            stat.userId = userId;
            stat.dateField = field;
            stats.put(field, stat);
        }
        return stat;
    }

    public UserTimeRatingStat getMonthStat(long month) {
        return get(this.monthStats, month);
    }

    public void integrate(Interest interest, InteractionLevel interactionLevel, long month) {
        this.ratingCount.integrate(interest, interactionLevel);
        UserTimeRatingStat monthStat = this.getMonthStat(month);
        monthStat.ratingCount.integrate(interest, interactionLevel);

    }

    public String toString(long minMonth, long maxMonth) {
        StringBuilder sb = new StringBuilder();

        sb.append(StringUtils.join(new String[] { userId, ratingCount.toString() }, " "));

        for (long i = minMonth; i <= maxMonth; i++) {
            UserTimeRatingStat stat = this.getMonthStat(i - minMonth);
            sb.append(" " + stat.ratingCount.toString());
        }
        return sb.toString();
    }

}