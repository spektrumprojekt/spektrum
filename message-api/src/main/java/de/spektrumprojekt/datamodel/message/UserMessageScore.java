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

package de.spektrumprojekt.datamodel.message;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Index;

import de.spektrumprojekt.datamodel.identifiable.SpektrumEntity;

/**
 * Contains the score of a message for a user
 * 
 * TODO move to some persistences package
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
@Entity
// @Table(uniqueConstraints = @UniqueConstraint(columnNames = { "messageGlobalId", "userGlobalId"
// }))
public class UserMessageScore implements SpektrumEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Index
    private float score;

    @Index
    @Id
    private String messageGlobalId;

    @Index
    @Id
    private String userGlobalId;

    @Transient
    private boolean author;

    @Index
    private InteractionLevel interactionLevel = InteractionLevel.UNKNOWN;

    @Transient
    private boolean basedOnAdaptedTerms;

    /**
     * for jpa
     */
    protected UserMessageScore() {

    }

    public UserMessageScore(String messageGlobalId, String userGlobalId) {
        this(messageGlobalId, userGlobalId, 0);
    }

    public UserMessageScore(String messageGlobalId, String userGlobalId, float score) {
        this(messageGlobalId, userGlobalId, null, score);
    }

    public UserMessageScore(String messageGlobalId, String userGlobalId,
            InteractionLevel interactionLevel, float score) {

        if (messageGlobalId == null) {
            throw new IllegalArgumentException("messageGlobalId cannot be null.");
        }
        if (userGlobalId == null) {
            throw new IllegalArgumentException("userGlobalId cannot be null.");
        }
        this.messageGlobalId = messageGlobalId;
        this.userGlobalId = userGlobalId;
        this.score = score;
        this.interactionLevel = interactionLevel;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UserMessageScore other = (UserMessageScore) obj;
        if (messageGlobalId == null) {
            if (other.messageGlobalId != null) {
                return false;
            }
        } else if (!messageGlobalId.equals(other.messageGlobalId)) {
            return false;
        }
        if (userGlobalId == null) {
            if (other.userGlobalId != null) {
                return false;
            }
        } else if (!userGlobalId.equals(other.userGlobalId)) {
            return false;
        }
        return true;
    }

    public InteractionLevel getInteractionLevel() {
        return interactionLevel;
    }

    /**
     * 
     * @return the global id of the message
     */
    public String getMessageGlobalId() {
        return messageGlobalId;
    }

    /**
     * 
     * @return the rank
     */
    public float getScore() {
        return score;
    }

    /**
     * 
     * @return the user global id
     */
    public String getUserGlobalId() {
        return userGlobalId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (messageGlobalId == null ? 0 : messageGlobalId.hashCode());
        result = prime * result + (userGlobalId == null ? 0 : userGlobalId.hashCode());
        return result;
    }

    public boolean isBasedOnAdaptedTerms() {
        return basedOnAdaptedTerms;
    }

    public void setBasedOnAdaptedTerms(boolean basedOnAdaptedTerms) {
        this.basedOnAdaptedTerms = basedOnAdaptedTerms;
    }

    public void setInteractionLevel(InteractionLevel interactionLevel) {
        if (interactionLevel == null) {
            throw new IllegalArgumentException("interactionLevel cannot be null");
        }
        this.interactionLevel = interactionLevel;
    }

    /**
     * 
     * @param messageGlobalId
     *            set the message id
     */
    public void setMessageGlobalId(String messageGlobalId) {
        this.messageGlobalId = messageGlobalId;
    }

    /**
     * 
     * @param rank
     *            the rank
     */
    public void setScore(float rank) {
        this.score = rank;
    }

    /**
     * 
     * @param userGlobalId
     *            the user global id
     */
    public void setUserGlobalId(String userGlobalId) {
        this.userGlobalId = userGlobalId;
    }

    @Override
    public String toString() {
        return "UserMessageScore [score=" + score + ", messageGlobalId=" + messageGlobalId
                + ", userGlobalId=" + userGlobalId + ", author=" + author + ", interactionLevel="
                + interactionLevel + ", basedOnAdaptedTerms=" + basedOnAdaptedTerms + "]";
    }

}
