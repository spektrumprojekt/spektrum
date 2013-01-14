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

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "userGlobalIdFrom", "userGlobalIdTo",
        "topicGlobalId" }))
public class UserSimilarity extends Identifiable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final String userGlobalIdFrom;
    private final String userGlobalIdTo;
    private final String messageGroupGlobalId;

    private double similarity;

    /**
     * for jpa
     */
    protected UserSimilarity() {
        // for jpa
        userGlobalIdFrom = null;
        userGlobalIdTo = null;
        messageGroupGlobalId = null;
    }

    public UserSimilarity(String userGlobalIdFrom, String userGlobalIdTo, String messageGroupGlobalId) {
        this(userGlobalIdFrom, userGlobalIdTo, messageGroupGlobalId, 0);
    }

    public UserSimilarity(String userGlobalIdFrom, String userGlobalIdTo,
            String messageGroupGlobalId,
            double similarity) {
        if (userGlobalIdFrom == null) {
            throw new IllegalArgumentException("userGlobalIdFrom cannot be null");
        }
        if (userGlobalIdTo == null) {
            throw new IllegalArgumentException("userGlobalIdTo cannot be null");
        }
        if (messageGroupGlobalId == null) {
            throw new IllegalArgumentException("messageGroupGlobalId cannot be null");
        }

        this.userGlobalIdFrom = userGlobalIdFrom;
        this.userGlobalIdTo = userGlobalIdTo;
        this.messageGroupGlobalId = messageGroupGlobalId;
        this.setSimilarity(similarity);
    }

    public String getMessageGroupGlobalId() {
        return messageGroupGlobalId;
    }

    public double getSimilarity() {
        return similarity;
    }

    public String getUserGlobalIdFrom() {
        return userGlobalIdFrom;
    }

    public String getUserGlobalIdTo() {
        return userGlobalIdTo;
    }

    public void setSimilarity(double similarity) {
        if (similarity < 0) {
            throw new IllegalArgumentException("similarity cannot be negative. similarity="
                    + similarity);
        }
        if (similarity > 1) {
            throw new IllegalArgumentException("similarity cannot be greater than 1. similarity="
                    + similarity);
        }
        this.similarity = similarity;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserSimilarity [userGlobalIdFrom=");
        builder.append(userGlobalIdFrom);
        builder.append(", userGlobalIdTo=");
        builder.append(userGlobalIdTo);
        builder.append(", messageGroupGlobalId=");
        builder.append(messageGroupGlobalId);
        builder.append(", similarity=");
        builder.append(similarity);
        builder.append(", getGlobalId()=");
        builder.append(getGlobalId());
        builder.append(", getId()=");
        builder.append(getId());
        builder.append("]");
        return builder.toString();
    }

}
