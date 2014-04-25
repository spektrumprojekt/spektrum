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
package de.spektrumprojekt.i.similarity.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.output.SpektrumParseableElementFileOutput;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.helper.IdentifiableHelper;
import de.spektrumprojekt.persistence.Persistence;

public class UserSimilarityOutput extends SpektrumParseableElementFileOutput<UserSimilarity> {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserSimilarityOutput.class);

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

    public UserSimilarityOutput() {
        super(UserSimilarity.class);
    }

    public UserSimilarityOutput(Persistence persistence) {
        this();
        this.loadFromPersistence(persistence);
    }

    @Override
    protected UserSimilarity createNewElement(String line) {
        return new UserSimilarity(line);
    }

    @Override
    protected String getHeader() {
        return UserSimilarity.getUserSimilarityHeader();
    }

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

                    this.getElements().add(sim);
                }
            }
        }

        // Collections.sort(users, comp);
        // Collections.sort(messageGroups, comp);

        LOGGER.debug("Loaded {} UserSimilarities from Persistence.", getElements().size());
    }

}