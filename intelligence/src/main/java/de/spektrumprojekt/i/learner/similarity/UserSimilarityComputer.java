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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.time.TimeProviderHolder;
import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.persistence.Persistence;

public class UserSimilarityComputer implements ConfigurationDescriptable {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserSimilarityComputer.class);

    private static final long MONTH = 28 * 24 * 3600;

    private long intervall = MONTH;

    private final Persistence persistence;

    private Map<String, Integer> overallMentionsPerUserFrom = new HashMap<String, Integer>();

    private Map<String, Integer> overallMentionsPerUserTo = new HashMap<String, Integer>();

    public UserSimilarityComputer(Persistence persistence) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        this.persistence = persistence;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " intervall: " + intervall;
    }

    public Collection<UserSimilarity> run() {

        overallMentionsPerUserFrom.clear();
        overallMentionsPerUserTo.clear();

        Map<String, UserSimilarity> similarities = new HashMap<String, UserSimilarity>();

        // 1st get a list of all users
        // skipped

        // 2nd get a list of all topics
        Collection<MessageGroup> messageGroups = persistence.getAllMessageGroups();

        // 3rd init user sim matrix
        // skipped

        // 4th iterate over message of last month (or whatever)
        long now = TimeProviderHolder.DEFAULT.getCurrentTime();
        Date goingBack = new Date(now - intervall);
        for (MessageGroup messageGroup : messageGroups) {
            // some iteration ?
            Collection<Message> messages = persistence.getMessagesSince(messageGroup.getGlobalId(),
                    goingBack);

            for (Message message : messages) {
                updateUserSimilarities(similarities, message);
            }
        }

        for (UserSimilarity stat : similarities.values()) {
            String userGlobalIdFrom = stat.getUserGlobalIdFrom();
            String userGlobalIdTo = stat.getUserGlobalIdTo();

            updateOverallCounts(userGlobalIdFrom, userGlobalIdTo);
        }

        // 5th compute similarity, topic based!
        for (UserSimilarity stat : similarities.values()) {
            UserSimilarity reverse = similarities.get(UserSimilarity.getKey(
                    stat.getUserGlobalIdTo(), stat.getUserGlobalIdFrom(),
                    stat.getMessageGroupGlobalId()));
            if (reverse == null) {
                reverse = new UserSimilarity(stat.getUserGlobalIdTo(), stat.getUserGlobalIdFrom(),
                        stat.getMessageGroupGlobalId());
            }
            stat.consolidate(reverse, overallMentionsPerUserFrom.get(stat.getUserGlobalIdFrom()),
                    overallMentionsPerUserTo.get(stat.getUserGlobalIdTo()));
        }
        Collection<UserSimilarity> userSimilarities = new HashSet<UserSimilarity>(
                similarities.values());
        persistence.deleteAndCreateUserSimilarities(userSimilarities);

        return userSimilarities;
    }

    public void runForMessage(Message message) {
        Collection<String> mentionUserGlobalIds = MessageHelper.getMentions(message);

        String messageGroupGlobalId = message.getMessageGroup() != null ? message.getMessageGroup()
                .getGlobalId() : null;
        String userGlobalIdFrom = message.getAuthorGlobalId();

        to: for (String userGlobalIdTo : mentionUserGlobalIds) {
            if (userGlobalIdFrom.equals(userGlobalIdTo)) {
                break to;
            }
            UserSimilarity stat = this.persistence.getUserSimilarity(userGlobalIdFrom,
                    userGlobalIdTo, messageGroupGlobalId);
            UserSimilarity reverse = this.persistence.getUserSimilarity(userGlobalIdTo,
                    userGlobalIdFrom, messageGroupGlobalId);
            if (stat == null) {
                stat = new UserSimilarity(userGlobalIdFrom, userGlobalIdTo,
                        messageGroupGlobalId);
            }
            if (reverse == null) {
                reverse = new UserSimilarity(userGlobalIdTo, userGlobalIdFrom,
                        messageGroupGlobalId);
            }
            stat.incrementNumberOfMentions();

            updateOverallCounts(userGlobalIdFrom, userGlobalIdTo);

            stat.consolidate(reverse, overallMentionsPerUserFrom.get(stat.getUserGlobalIdFrom()),
                    overallMentionsPerUserTo.get(stat.getUserGlobalIdTo()));

            this.persistence.storeUserSimilarity(stat);
            this.persistence.storeUserSimilarity(reverse);
        }
    }

    private void updateOverallCounts(String userGlobalIdFrom, String userGlobalIdTo) {
        Integer from = overallMentionsPerUserFrom.get(userGlobalIdFrom);
        if (from == null) {
            from = 0;
        }
        from++;
        overallMentionsPerUserFrom.put(userGlobalIdFrom, from);

        Integer to = overallMentionsPerUserFrom.get(userGlobalIdTo);
        if (to == null) {
            to = 0;
        }
        to++;
        overallMentionsPerUserTo.put(userGlobalIdTo, to);
    }

    private void updateUserSimilarities(Map<String, UserSimilarity> similarities,
            Message message) {
        Collection<String> mentionUserGlobalIds = MessageHelper.getMentions(message);

        String messageGroupId = message.getMessageGroup() != null ? message.getMessageGroup()
                .getGlobalId() : null;
        String from = message.getAuthorGlobalId();

        to: for (String to : mentionUserGlobalIds) {
            if (from.equals(to)) {
                break to;
            }
            String key = UserSimilarity.getKey(from, to,
                    messageGroupId);
            UserSimilarity stat = similarities.get(key);
            if (stat == null) {
                stat = new UserSimilarity(from, to, messageGroupId);
                similarities.put(stat.getKey(), stat);
            }
            stat.incrementNumberOfMentions();
        }

    }
}
