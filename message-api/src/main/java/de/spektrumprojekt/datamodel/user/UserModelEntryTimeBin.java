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

package de.spektrumprojekt.datamodel.user;

import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;

@Entity
public class UserModelEntryTimeBin extends Identifiable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Sort the time bin entries by their start date, oldest first.
     * 
     * @param entries
     */
    public static void sort(List<UserModelEntryTimeBin> entries) {
        Collections.sort(entries, UserModelEntryTimeBinComparator.INSTANCE);
    }

    private long timeBinStart;

    private float scoreCount;

    private float scoreSum;

    /**
     * For JPA
     */
    protected UserModelEntryTimeBin() {
        // nothing to do
    }

    public UserModelEntryTimeBin(long timeBinStart) {
        this.timeBinStart = timeBinStart;
    }

    public float getScoreCount() {
        return scoreCount;
    }

    public float getScoreSum() {
        return scoreSum;
    }

    public long getTimeBinStart() {
        return timeBinStart;
    }

    public void setScoreCount(float scoreCount) {
        this.scoreCount = scoreCount;
    }

    public void setScoreSum(float scoreSum) {
        this.scoreSum = scoreSum;
    }

}
