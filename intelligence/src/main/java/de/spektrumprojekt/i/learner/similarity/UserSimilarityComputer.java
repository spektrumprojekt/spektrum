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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.persistence.Persistence;

public class UserSimilarityComputer implements ConfigurationDescriptable {

    private static final long MONTH = 28 * 24 * 3600;

    private long intervall = MONTH;

    private final Persistence persistence;

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

        Map<String, UserSimilarityStat> similarities = new HashMap<String, UserSimilarityStat>();

        // 1st get a list of all users

        List<User> users = new ArrayList<User>(persistence.getAllUsers());

        // 2nd get a list of all topics
        Collection<MessageGroup> messageGroups = persistence.getAllMessageGroups();

        // 3rd init user sim matrix
        for (MessageGroup messageGroup : messageGroups) {
            for (int i = 0; i < users.size(); i++) {
                inner: for (int j = 0; j < users.size(); j++) {
                    if (i == j) {
                        break inner;
                    }
                    UserSimilarityStat stat1 = new UserSimilarityStat(users.get(i).getGlobalId(),
                            users.get(j)
                                    .getGlobalId(), messageGroup.getGlobalId());
                    UserSimilarityStat stat2 = new UserSimilarityStat(users.get(j).getGlobalId(),
                            users.get(i)
                                    .getGlobalId(), messageGroup.getGlobalId());
                    similarities.put(stat1.getKey(), stat1);
                    similarities.put(stat2.getKey(), stat2);
                }
            }
        }

        // 4th iterate over message of last month (or whatever)
        Date now = new Date();
        Date goingBack = new Date(now.getTime() - intervall);
        for (MessageGroup messageGroup : messageGroups) {
            // some iteration ?
            Collection<Message> messages = persistence.getMessagesSince(messageGroup.getGlobalId(),
                    goingBack);

            for (Message message : messages) {
                updateUserSimilarities(similarities, message);
            }
        }

        Map<String, Integer> overallMentionsPerUserFrom = new HashMap<String, Integer>();
        Map<String, Integer> overallMentionsPerUserTo = new HashMap<String, Integer>();

        for (UserSimilarityStat stat : similarities.values()) {
            Integer from = overallMentionsPerUserFrom.get(stat.getUserGlobalIdFrom());
            if (from == null) {
                from = 0;
            }
            from++;
            overallMentionsPerUserFrom.put(stat.getUserGlobalIdFrom(), from);

            Integer to = overallMentionsPerUserFrom.get(stat.getUserGlobalIdTo());
            if (to == null) {
                to = 0;
            }
            to++;
            overallMentionsPerUserTo.put(stat.getUserGlobalIdFrom(), to);
        }

        // 5th compute similarity, topic based!
        for (UserSimilarityStat stat : similarities.values()) {
            UserSimilarityStat reverse = similarities.get(UserSimilarityStat.getKey(
                    stat.getUserGlobalIdTo(), stat.getUserGlobalIdFrom(),
                    stat.getMessageGroupGlobalId()));
            assert reverse != null;
            stat.consolidate(reverse, overallMentionsPerUserFrom.get(stat.getUserGlobalIdFrom()),
                    overallMentionsPerUserTo.get(stat.getUserGlobalIdTo()));
        }
        Collection<UserSimilarity> userSimilarities = new HashSet<UserSimilarity>(
                similarities.values());
        persistence.deleteAndCreateUserSimilarities(userSimilarities);

        return userSimilarities;
    }

    private void updateUserSimilarities(Map<String, UserSimilarityStat> similarities,
            Message message) {
        Collection<String> mentionUserGlobalIds = MessageHelper.getMentions(message);

        String messageGroupId = message.getMessageGroup() != null ? message.getMessageGroup()
                .getGlobalId() : null;
        String from = message.getAuthorGlobalId();

        to: for (String to : mentionUserGlobalIds) {
            if (from.equals(to)) {
                break to;
            }
            UserSimilarityStat stat = similarities.get(UserSimilarityStat.getKey(from, to,
                    messageGroupId));
            assert stat != null;
            stat.incrementNumberOfMentions();
        }

    }
}
