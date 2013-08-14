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
package de.spektrumprojekt.i.user.similarity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.helper.IdentifiableHelper;
import de.spektrumprojekt.persistence.Persistence;

public class UserSimilarityOutput {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserSimilarityOutput.class);

    private final List<UserSimilarity> userSimilarities = new ArrayList<UserSimilarity>();

    private final static Comparator<Identifiable> comp = new Comparator<Identifiable>() {

        @Override
        public int compare(Identifiable o1, Identifiable o2) {
            return o1.getGlobalId().compareTo(o2.getGlobalId());
        }

    };

    private final static Comparator<UserSimilarity> compSim = new Comparator<UserSimilarity>() {

        @Override
        public int compare(UserSimilarity o1, UserSimilarity o2) {
            int result = o1.getUserGlobalIdFrom().compareTo(o2.getUserGlobalIdFrom());
            if (result == 0) {
                result = o1.getUserGlobalIdTo().compareTo(o2.getUserGlobalIdTo());
            }
            return result;
        }

    };

    private List<String> comments = new ArrayList<String>();

    public UserSimilarityOutput(File file) throws IOException {
        this.readFromFile(file);
    }

    public UserSimilarityOutput(Persistence persistence) {
        this.loadFromPersistence(persistence);
    }

    public List<UserSimilarity> getUserSimilarities() {
        return userSimilarities;
    }

    /*
     * public List<String> dumpUserSimilarities() {
     * 
     * // header
     * 
     * for (User user : users) { Integer from =
     * this.overallMentionsPerUserFrom.get(user.getGlobalId()); Integer to =
     * this.overallMentionsPerUserTo.get(user.getGlobalId());
     * 
     * if (from != null || to != null) { if (from == null) { from = 0; } if (to == null) { to = 0; }
     * 
     * rows.add("overallMentions " + user.getGlobalId() + " from ->: " + from + "  to <-: " + to); }
     * }
     * 
     * LOGGER.info("Dumping UserSimilarities done with {} rows." + rows.size());
     * 
     * return rows; }
     */

    private void loadFromPersistence(Persistence persistence) {

        LOGGER.debug("Load UserSimilarities from Persistence...");

        List<User> users = new ArrayList<User>(persistence.getAllUsers());
        List<MessageGroup> messageGroups = new ArrayList<MessageGroup>(
                persistence.getAllMessageGroups());

        Collection<String> userIds = IdentifiableHelper.getGlobalIds(users);

        for (MessageGroup messageGroup : messageGroups) {
            for (User user : persistence.getAllUsers()) {
                List<UserSimilarity> sims = new ArrayList<UserSimilarity>(
                        persistence.getUserSimilarities(
                                user.getGlobalId(), userIds, messageGroup.getGlobalId(), 0.01d));
                Collections.sort(sims, compSim);

                for (UserSimilarity sim : sims) {

                    this.userSimilarities.add(sim);
                }
            }
        }

        // Collections.sort(users, comp);
        // Collections.sort(messageGroups, comp);

        LOGGER.debug("Loaded {} UserSimilarities from Persistence.", userSimilarities.size());
    }

    private void readFromFile(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null.");
        }
        LOGGER.debug("Reading UserSimilarities from " + file.getName());

        List<String> rows = FileUtils.readLines(file);

        for (String row : rows) {
            row = row.trim();
            if (row.startsWith("#")) {
                this.comments.add(row);
            } else {
                UserSimilarity userSimilarity = new UserSimilarity(row);
                this.userSimilarities.add(userSimilarity);
            }
        }

        LOGGER.info("Read " + this.userSimilarities.size() + " from " + file.getName());
    }

    public void write(File file) throws IOException {
        List<String> rows = new ArrayList<String>();
        rows.add(UserSimilarity.getUserSimilarityHeader());
        for (String desc : this.comments) {
            if (!desc.startsWith("#")) {
                desc = "#" + desc;
            }
            rows.add(desc);
        }

        for (UserSimilarity userSimilarity : this.userSimilarities) {
            rows.add(userSimilarity.toParseableString());
        }

        FileUtils.writeLines(file, rows);

        LOGGER.info("Wrote " + this.userSimilarities.size() + " to " + file.getName());
    }

}