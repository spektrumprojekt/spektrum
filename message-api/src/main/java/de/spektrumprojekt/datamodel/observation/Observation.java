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
package de.spektrumprojekt.datamodel.observation;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.eclipse.persistence.annotations.Index;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.user.User;

/**
 * An object of this class stores some observation made about an user. There is a type of
 * observation {@link ObservationType} defining what kind of observation was made. Based on the type
 * the {@link #observation} itself should store the information, e.g. the id of the message that was
 * stored or liked.
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public class Observation extends Identifiable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Index
    private String userGlobalId;;

    @Index
    private String messageGlobalId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date observationDate;

    private ObservationType observationType;

    private ObservationPriority priority;

    private String observation;

    // a derived interest, if null it will be determined by observation type
    private Interest interest;

    /**
     * for jpa
     */
    protected Observation() {
        // for jpa
    }

    public Observation(String userGlobalId,
            String messageGlobalId,
            ObservationType observationType,
            ObservationPriority priority,
            String observation,
            Date observationDate,
            Interest interest) {
        if (userGlobalId == null) {
            throw new IllegalArgumentException("userGlobalId cannot be null");
        }
        if (messageGlobalId == null) {
            throw new IllegalArgumentException("messageGlobalId cannot be null");
        }
        if (observationType == null) {
            throw new IllegalArgumentException("observationType cannot be null");
        }
        if (priority == null) {
            throw new IllegalArgumentException("priority cannot be null");
        }
        if (observationDate == null) {
            observationDate = new Date();
        }
        this.userGlobalId = userGlobalId;
        this.messageGlobalId = messageGlobalId;
        this.observationType = observationType;
        this.priority = priority;
        this.observation = observation;
        this.observationDate = observationDate;
        this.interest = interest;
    }

    public Observation(User user,
            Message message,
            ObservationType observationType,
            ObservationPriority priority,
            String observation,
            Date observationDate,
            Interest interest) {
        this(user == null ? null : user.getGlobalId(),
                message == null ? null : message.getGlobalId(),
                observationType,
                priority,
                observation,
                observationDate, interest);
    }

    public Observation(User user,
            ObservationType observationType,
            ObservationPriority priority,
            String observation) {
        this(user, null, observationType, priority, observation, null, null);
    }

    public Interest getInterest() {
        return interest;
    }

    public String getMessageGlobalId() {
        return messageGlobalId;
    }

    public String getObservation() {
        return observation;
    }

    public Date getObservationDate() {
        return observationDate;
    }

    public ObservationType getObservationType() {
        return observationType;
    }

    public ObservationPriority getPriority() {
        return priority;
    }

    public String getUserGlobalId() {
        return userGlobalId;
    }

    public void setInterest(Interest interest) {
        this.interest = interest;
    }

    public void setMessageGlobalId(String messageGlobalId) {
        this.messageGlobalId = messageGlobalId;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public void setObservationDate(Date observationDate) {
        this.observationDate = observationDate;
    }

    public void setObservationType(ObservationType observationType) {
        this.observationType = observationType;
    }

    public void setPriority(ObservationPriority priority) {
        this.priority = priority;
    }

    public void setUserGlobalId(String userGlobalId) {
        this.userGlobalId = userGlobalId;
    }

    @Override
    public String toString() {
        return "Observation [userGlobalId=" + userGlobalId + ", messageGlobalId=" + messageGlobalId
                + ", observationDate=" + observationDate + ", observationType=" + observationType
                + ", priority=" + priority + ", observation=" + observation + ", interest="
                + interest + "]";
    }
}
