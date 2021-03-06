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

package de.spektrumprojekt.commons.time;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class ManualTimeProvider implements TimeProvider {

    private long currentTime;

    @Override
    public long getCurrentTime() {
        return currentTime;
    }

    public void reset() {
        this.currentTime = 0;
    }

    public void setCurrentTime(long currentTime) {
        if (currentTime < this.currentTime) {
            throw new IllegalArgumentException(
                    "Time travel still not invented. Will not set time to: " + currentTime
                            + " Currently set: " + this.currentTime);
        }
        this.currentTime = currentTime;
    }

}
