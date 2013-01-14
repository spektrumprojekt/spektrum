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

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;
import de.spektrumprojekt.datamodel.message.ScoredTerm;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public class UserModelEntry extends Identifiable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    private ScoredTerm scoredTerm;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    private Collection<UserModelEntryTimeBin> timeBinEntries;

    @ManyToOne(optional = false)
    private UserModel userModel;

    private float scoreCount;

    private float scoreSum;

    private transient boolean adapted;

    /**
     * for the jpa
     */
    protected UserModelEntry() {
        // nothing to do
    }

    public UserModelEntry(UserModel userModel, ScoredTerm scoredTerm) {
        this.scoredTerm = scoredTerm;
        this.userModel = userModel;
    }

    public void addTimeBinEntry(UserModelEntryTimeBin timeBin) {
        if (timeBin == null) {
            throw new IllegalArgumentException("timeBin cannot be null");
        }
        if (this.getUserModelEntryTimeBinByStartTime(timeBin.getTimeBinStart()) != null) {
            throw new IllegalStateException("timeBin with index "
                    + timeBin.getTimeBinStart()
                    + " already exits. existingTimeBin="
                    + getUserModelEntryTimeBinByStartTime(timeBin.getTimeBinStart())
                    + " newTimeBin="
                    + timeBin
                    + " this=" + this);
        }
        if (this.timeBinEntries == null) {
            this.timeBinEntries = new HashSet<UserModelEntryTimeBin>();
        }
        this.timeBinEntries.add(timeBin);
    }

    public void consolidate() {
        if (!this.adapted) {
            this.scoredTerm.setWeight(this.scoreSum / this.scoreCount);
        }
    }

    public void consolidateByTimeBins() {
        float count = 0, sum = 0;
        if (this.timeBinEntries != null) {
            for (UserModelEntryTimeBin timeBin : this.timeBinEntries) {
                count += timeBin.getScoreCount();
                sum += timeBin.getScoreSum();
            }
        }
        this.scoreCount = count;
        this.scoreSum = sum;
        consolidate();
    }

    public float getScoreCount() {
        return scoreCount;
    }

    public ScoredTerm getScoredTerm() {
        return scoredTerm;
    }

    public float getScoreSum() {
        return scoreSum;
    }

    public Collection<UserModelEntryTimeBin> getTimeBinEntries() {
        return timeBinEntries;
    }

    public UserModelEntryTimeBin getUserModelEntryTimeBinByStartTime(long timeBinStartTime) {
        if (this.timeBinEntries == null) {
            return null;
        }
        for (UserModelEntryTimeBin timeBin : this.timeBinEntries) {
            if (timeBin.getTimeBinStart() == timeBinStartTime) {
                return timeBin;
            }
        }
        return null;
    }

    public boolean isAdapted() {
        return adapted;
    }

    public UserModelEntryTimeBin removeUserModelEntryTimeBin(long timeBinStartTime) {
        UserModelEntryTimeBin timeBin = getUserModelEntryTimeBinByStartTime(timeBinStartTime);
        if (timeBin != null) {
            this.timeBinEntries.remove(timeBin);
        }
        return timeBin;
    }

    public void setAdapted(boolean adapted) {
        this.adapted = adapted;
    }

    public void setScoreCount(float scoreCount) {
        this.scoreCount = scoreCount;
    }

    public void setScoredTerm(ScoredTerm scoredTerm) {
        this.scoredTerm = scoredTerm;
    }

    public void setScoreSum(float scoreSum) {
        this.scoreSum = scoreSum;
    }

    @Override
    public String toString() {
        return "UserModelEntry [scoredTerm=" + scoredTerm + ", userModel=" + userModel
                + ", scoreCount=" + scoreCount + ", scoreSum=" + scoreSum + ", timeBins.size="
                + (timeBinEntries == null ? 0 : timeBinEntries.size()) + "]";
    }
}
