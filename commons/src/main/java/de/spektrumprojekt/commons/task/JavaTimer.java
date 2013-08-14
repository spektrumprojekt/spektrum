package de.spektrumprojekt.commons.task;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.time.DateUtils;

/**
 * A java timer that will invoke the task runner based on the intervall given (by default every
 * hour). The timer will just do a check on the {@link TaskRunner} that will then execute the task
 * that are necessary. So the intervall determines how often it should be check to execute a task.
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class JavaTimer {

    private final TaskRunner taskRunner;
    private final long intervall;

    private TimerTask timerTask;
    private Timer timer;

    public JavaTimer(TaskRunner taskRunner) {
        this(taskRunner, DateUtils.MILLIS_PER_HOUR);
    }

    /**
     * 
     * @param taskRunner
     * @param intervall
     *            intervall to check for tasks to execute
     */
    public JavaTimer(TaskRunner taskRunner, long intervall) {
        if (taskRunner == null) {
            throw new IllegalArgumentException("taskRunner cannot be null.");
        }
        this.taskRunner = taskRunner;
        this.intervall = intervall;
    }

    public void start() {
        stop();

        timerTask = new TimerTask() {

            @Override
            public void run() {
                taskRunner.check();
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, intervall);

    }

    public void stop() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
    }
}