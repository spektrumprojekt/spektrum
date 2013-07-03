package de.spektrumprojekt.commons.time;

import de.spektrumprojekt.commons.task.TaskRunner;

/**
 * TimeProvider that will use a {@link TaskRunner} and invoke it every time a new time is set. The
 * {@link TaskRunner} will then execute the tasks that should run next.
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class ManualTaskRunningTimeProvider extends ManualTimeProvider {

    private final TaskRunner taskRunner;

    public ManualTaskRunningTimeProvider(TaskRunner taskRunner) {
        if (taskRunner == null) {
            throw new IllegalArgumentException("taskRunner cannot be null.");
        }
        this.taskRunner = taskRunner;
    }

    @Override
    public void setCurrentTime(long currentTime) {
        super.setCurrentTime(currentTime);

        this.taskRunner.check();
    }

}