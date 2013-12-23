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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

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
import de.spektrumprojekt.datamodel.message.Term;

/**
 * A user model entry with a term. The actual value to be used is stored in the {@link #scoredTerm}
 * {@link ScoredTerm#getWeight()}. The {@link #scoreCount} keeps number of scores add to
 * {@link #scoreSum}. The flag {@link #adapted} tells that the weight of the scored term is not
 * derived by the sum and count of the entry but from somewhere else (e.g. from other user models or
 * somewhere else).
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

    public static Map<Term, UserModelEntry> createTermToEntryMap(Collection<UserModelEntry> entries) {
        Map<Term, UserModelEntry> map = new HashMap<Term, UserModelEntry>();
        for (UserModelEntry entry : entries) {
            map.put(entry.getScoredTerm().getTerm(), entry);
        }
        return map;
    }

    /**
     * Filter the map to only return term to {@link UserModelEntry} pairs that are NOT adapted.
     * 
     * @param entries
     * @return
     */
    public static Map<Term, UserModelEntry> filteredForNonAdaptedEntries(
            Map<Term, UserModelEntry> entries) {
        final Map<Term, UserModelEntry> filteredEntries = new HashMap<Term, UserModelEntry>();
        for (Entry<Term, UserModelEntry> entry : entries.entrySet()) {
            if (!entry.getValue().isAdapted()) {
                filteredEntries.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredEntries;
    }

    @OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    private ScoredTerm scoredTerm;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    private Collection<UserModelEntryTimeBin> timeBinEntries;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    private final Collection<UserModelEntryTimeBin> timeBinEntriesHistory = new HashSet<UserModelEntryTimeBin>();;

    @ManyToOne(optional = false)
    private UserModel userModel;

    private float scoreCount;

    private float scoreSum;

    private transient boolean adapted;

    private transient Date lastChange;

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
            throw new IllegalStateException("timeBin with index " + timeBin.getTimeBinStart()
                    + " already exits. existingTimeBin="
                    + getUserModelEntryTimeBinByStartTime(timeBin.getTimeBinStart())
                    + " newTimeBin=" + timeBin + " this=" + this);
        }
        if (this.timeBinEntries == null) {
            this.timeBinEntries = new HashSet<UserModelEntryTimeBin>();
        }
        this.timeBinEntries.add(timeBin);
    }

    public boolean addToTimeBinEntriesHistory(UserModelEntryTimeBin e) {
        return timeBinEntriesHistory.add(e);
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

    public Date getLastChange() {
        return lastChange;
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

    public Collection<UserModelEntryTimeBin> getTimeBinEntriesHistory() {
        return timeBinEntriesHistory;
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

    public void setLastChange(Date lastChange) {
        this.lastChange = lastChange;
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
