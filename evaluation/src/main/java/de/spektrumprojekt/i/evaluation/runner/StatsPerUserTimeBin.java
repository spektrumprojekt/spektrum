package de.spektrumprojekt.i.evaluation.runner;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.SimpleType;

import de.spektrumprojekt.i.evaluation.measure.EvaluatorDataPoint;

/**
 * Holds statistischs for each user per time bin
 * 
 * @author Torsten
 * 
 */
public class StatsPerUserTimeBin {

    public static class UserTimeBinStats {
        private int numberRatings;
        private int numberPositiveReferences;

        public int getNumberPositiveReferences() {
            return numberPositiveReferences;
        }

        public int getNumberRatings() {
            return numberRatings;
        }

        public void setNumberPositiveReferences(int numberPositiveReferences) {
            this.numberPositiveReferences = numberPositiveReferences;
        }

        public void setNumberRatings(int numberRatings) {
            this.numberRatings = numberRatings;
        }
    }

    public static StatsPerUserTimeBin getFromComments(Collection<String> comments)
            throws IOException {
        for (String comment : comments) {
            int index = comment.indexOf("statsPerUserTimeBin:");
            if (index >= 0) {
                comment = comment.substring(index).replace("statsPerUserTimeBin:", "");
                StatsPerUserTimeBin stats = new StatsPerUserTimeBin();
                stats.fromJson(comment);
                return stats;
            }
        }
        return null;
    }

    public static String getKey(String userId, int timeBin) {
        return timeBin + "$$$" + userId;
    }

    public static int getTimeBinOfKey(String key) {
        return Integer.parseInt(key.split("$$$")[0]);
    }

    public static String getUserIdOfKey(String key) {
        return key.split("$$$")[1];
    }

    private final Map<String, UserTimeBinStats> stats = new HashMap<String, UserTimeBinStats>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final transient MapType MAP_TYPE = MapType
            .construct(
                    HashMap.class,
                    SimpleType.construct(String.class),
                    SimpleType.construct(UserTimeBinStats.class));

    public void compute(Collection<EvaluatorDataPoint> dataPoints) {

        for (EvaluatorDataPoint dataPoint : dataPoints) {
            String key = getKey(dataPoint.getUserGlobalId(), dataPoint.getTimeBin());
            UserTimeBinStats stat = stats.get(key);
            if (stat == null) {
                stat = new UserTimeBinStats();
                stats.put(key, stat);
            }
            stat.numberRatings++;
            if (dataPoint.getTarget() == 1d) {
                stat.numberPositiveReferences++;
            }
        }
    }

    public void fromJson(String json) throws IOException {

        Map<String, UserTimeBinStats> loadedStats = objectMapper.readValue(json, MAP_TYPE);
        this.stats.putAll(loadedStats);
    }

    public UserTimeBinStats get(String userId, int timeBin) {
        return this.stats.get(getKey(userId, timeBin));
    }

    public String toComment() throws IOException {
        return "# statsPerUserTimeBin: " + this.toJson();
    }

    public String toJson() throws IOException {
        return objectMapper.writeValueAsString(stats);
    }
}