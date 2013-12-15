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

import junit.framework.Assert;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.commons.computer.Computer;
import de.spektrumprojekt.commons.time.ManualTaskRunningTimeProvider;
import de.spektrumprojekt.commons.time.TimeProviderHolder;

/**
 * Test the task runner
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class TaskRunnerTest {

    public class DummyComputer implements Computer {
        private int value = 0;

        @Override
        public String getConfigurationDescription() {
            return this.getClass().getName();
        }

        public int getValue() {
            return value;
        }

        @Override
        public void run() {
            value += 1;
        }
    }

    private DummyComputer dummyComputer;
    private TaskRunner taskRunner;
    private ManualTaskRunningTimeProvider manualTaskRunningTimeProvider;

    private final long startDate = 0;

    @Before
    public void setup() {
        this.taskRunner = new TaskRunner(true);
        this.dummyComputer = new DummyComputer();

        this.manualTaskRunningTimeProvider = new ManualTaskRunningTimeProvider(this.taskRunner);
        TimeProviderHolder.DEFAULT = this.manualTaskRunningTimeProvider;
        this.manualTaskRunningTimeProvider.setCurrentTime(startDate);
    }

    /**
     * Test
     * 
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testTaskRunner() throws Exception {

        this.taskRunner.register(dummyComputer, DateUtils.MILLIS_PER_HOUR, 0, false);

        long time = this.startDate;
        for (int i = 0; i < 1000; i++) {
            time += DateUtils.MILLIS_PER_HOUR;
            this.manualTaskRunningTimeProvider.setCurrentTime(time);

            Assert.assertEquals(i + 1, this.dummyComputer.getValue());
        }

    }

    /**
     * Test
     * 
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testTaskRunnerDelay() throws Exception {

        this.taskRunner.register(dummyComputer, DateUtils.MILLIS_PER_HOUR,
                DateUtils.MILLIS_PER_HOUR, false);

        long time = this.startDate;
        for (int i = 0; i < 100; i++) {
            time += i == 0 ? DateUtils.MILLIS_PER_HOUR - 1 : DateUtils.MILLIS_PER_HOUR;
            this.manualTaskRunningTimeProvider.setCurrentTime(time);

            Assert.assertEquals(i, this.dummyComputer.getValue());
        }

    }

    /**
     * Test
     * 
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testTaskRunnerDelayExact() throws Exception {

        this.taskRunner.register(dummyComputer, DateUtils.MILLIS_PER_HOUR,
                DateUtils.MILLIS_PER_HOUR, true);

        long time = this.startDate;
        for (int i = 0; i < 100; i++) {
            time += DateUtils.MILLIS_PER_HOUR - 1;
            this.manualTaskRunningTimeProvider.setCurrentTime(time);

            Assert.assertEquals(i, this.dummyComputer.getValue());
        }

    }
}
