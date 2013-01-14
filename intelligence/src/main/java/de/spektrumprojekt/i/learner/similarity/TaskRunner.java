package de.spektrumprojekt.i.learner.similarity;

import java.util.Timer;
import java.util.TimerTask;

import de.spektrumprojekt.persistence.Persistence;

public class TaskRunner {

    private final Persistence persistence;
    private final UserSimilarityComputer userSimilarityComputer;

    private final static long DAY = 24 * 3600;

    public TaskRunner(Persistence persistence) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        this.persistence = persistence;
        userSimilarityComputer = new UserSimilarityComputer(this.persistence);
    }

    public void setup() {

        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                userSimilarityComputer.run();
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, DAY);
    }
}
