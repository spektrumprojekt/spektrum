package de.spektrumprojekt.i.scorer.threshold;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;

import de.spektrumprojekt.commons.computer.Computer;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.persistence.Persistence;

public class RelevanceScoreThresholdComputer implements Computer {

    private final Persistence persistence;

    private final int n;

    private final long timeIntervall = 7 * DateUtils.MILLIS_PER_DAY;

    private final Map<String, Float> userToThreshold = new HashMap<String, Float>();

    private final float minimumRelevantThreshold = 0.1f;

    public RelevanceScoreThresholdComputer(Persistence persistence, int n) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (n <= 0) {
            throw new IllegalArgumentException("n must be >= 0 but is: " + n);
        }
        this.persistence = persistence;
        this.n = n;
    }

    @Override
    public String getConfigurationDescription() {
        return RelevanceScoreThresholdComputer.class.getSimpleName()
                + " n: " + n
                + " minimumRelevantThreshold: " + minimumRelevantThreshold
                + " timeIntervall (in days): " + timeIntervall / DateUtils.MILLIS_PER_DAY;

    }

    public Map<String, Float> getUserToThreshold() {
        return userToThreshold;
    }

    @Override
    public synchronized void run() throws Exception {
        Date firstDate = new Date(new Date().getTime() - timeIntervall);

        userToThreshold.clear();
        for (User user : persistence.getAllUsers()) {

            UserMessageScore ums = this.persistence.getNthUserMessageScore(user.getGlobalId(), n,
                    firstDate);
            float score = ums == null ? minimumRelevantThreshold : ums.getScore();
            userToThreshold.put(user.getGlobalId(), score);

        }
    }

    @Override
    public String toString() {
        return this.getConfigurationDescription();
    }
}
