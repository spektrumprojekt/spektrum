/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.spektrumprojekt.commons.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.computer.Computer;
import de.spektrumprojekt.commons.time.TimeProviderHolder;

/**
 * Keeps a set of tasks. By calling the {@link #check()} method all tasks that are due will be
 * executed. For determining the current time {@link TimeProviderHolder#DEFAULT} will be used.
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class TaskRunner {

    private final List<Task> tasks = new ArrayList<Task>();

    private final static Logger LOGGER = LoggerFactory.getLogger(TaskRunner.class);

    private boolean exitOnError;

    public TaskRunner(boolean exitOnError) {

    }

    /**
     * Check the tasks that are due and run them if so
     */
    public void check() {
        for (Task task : this.tasks) {
            if (task.getNextDate().getTime() <= TimeProviderHolder.DEFAULT.getCurrentTime()) {
                try {
                    LOGGER.info(new Date(TimeProviderHolder.DEFAULT.getCurrentTime())
                            + " Running computer: "
                            + task.getComputer().getConfigurationDescription());
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start();
                    task.getComputer().run();
                    stopWatch.stop();
                    LOGGER.info("Finished computer in success in  " + stopWatch.getTime()
                            + " ms. " + task.getComputer().getClass().getName());
                } catch (Exception e) {
                    LOGGER.error("Error running task=" + task + " " + e.getMessage(), e);
                    if (exitOnError) {
                        System.exit(1000);
                    }
                }
                task.incrementNextDate();
            }
        }
    }

    /**
     * Register the computer for execution. See also {@link Task} for more details.
     * 
     * @param computer
     * @param intervall
     * @param delayOnFirstStart
     * @param runExactIntervall
     */
    public void register(Computer computer, long intervall, long delayOnFirstStart,
            boolean runExactIntervall) {
        Task task = new Task(computer, intervall, delayOnFirstStart, runExactIntervall);
        this.tasks.add(task);
    }
}
