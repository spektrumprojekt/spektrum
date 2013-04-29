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

package de.spektrumprojekt.i.ranker.chain;

import java.util.Collection;
import java.util.HashSet;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.MessageRank;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.persistence.Persistence;

/**
 * A command to store the message ranks
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class StoreMessageRankCommand implements Command<MessageFeatureContext> {

    private final Persistence persistence;

    /**
     * 
     * @param persistence
     *            the persistence to use
     */
    public StoreMessageRankCommand(Persistence persistence) {
        this.persistence = persistence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(MessageFeatureContext context) {
        if (context.isNoRankingOnlyLearning()) {
            return;
        }
        Collection<MessageRank> ranks = new HashSet<MessageRank>();
        for (UserSpecificMessageFeatureContext userContext : context.getUserContexts()) {
            if (userContext.getMessageRank() != null) {
                ranks.add(userContext.getMessageRank());
            }

            for (MessageRank rankToUpdate : userContext.getRanksToUpdate()) {

                persistence.updateMessageRank(rankToUpdate);
            }
        }

        if (!ranks.isEmpty()) {
            persistence.storeMessageRanks(ranks);
        }
    }
}
