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

package de.spektrumprojekt.i.learner.similarity;

import org.apache.commons.lang3.StringUtils;

import de.spektrumprojekt.datamodel.user.UserSimilarity;

public class UserSimilarityStat extends UserSimilarity {

    public static String getKey(String fromUserGlobalId, String toUserGlobalId,
            String messageGroupId) {
        return StringUtils.join(new Object[] { fromUserGlobalId, toUserGlobalId,
                messageGroupId }, "#");

    }

    private int numberOfMentions;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UserSimilarityStat(String userGlobalIdFrom, String userGlobalIdTo,
            String topicGlobalId) {
        super(userGlobalIdFrom, userGlobalIdTo, topicGlobalId, 0);
    }

    public void consolidate() {
        // TODO set the similarity
    }

    public String getKey() {
        return getKey(getUserGlobalIdFrom(), getUserGlobalIdTo(), getMessageGroupGlobalId());

    }

    public int getNumberOfMentions() {
        return numberOfMentions;
    }

    public void incrementNumberOfMentions() {
        numberOfMentions++;
    }

    public void setNumberOfMentions(int numberOfMentions) {
        this.numberOfMentions = numberOfMentions;
    }
}