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

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.persistence.Persistence;

/**
 * Compute if the message is the root of the discussion or not
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class StoreMessageCommand implements Command<MessageFeatureContext> {

    private Persistence persistence;

    /**
     * 
     * @param persistence
     *            the persistence to use
     */
    public StoreMessageCommand(Persistence persistence) {
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

        Message message = context.getMessage();

        // setTerms(message);

        // only store if it does not yet exist ?
        // Message existing = persistence.getMessageByGlobalId(message.getGlobalId());
        // if (existing == null) {
        if (message.getMessageGroup() != null) {
            MessageGroup current = message.getMessageGroup();
            MessageGroup persisted = this.persistence.getMessageGroupByGlobalId(message
                    .getMessageGroup().getGlobalId());
            if (persisted == null) {
                persisted = this.persistence.storeMessageGroup(current);
            }
            message.setMessageGroup(persisted);
        }
        persistence.storeMessage(message);
        // }

        if (context.getMessageRelation() != null) {

            persistence.storeMessageRelation(message, context.getMessageRelation());
        }

    }

    /**
     * Set persisted terms on the message
     * 
     * @param message
     *            the message
     */
    private void setTerms(Message message) {
        for (MessagePart messagePart : message.getMessageParts()) {
            for (ScoredTerm scoredTerm : messagePart.getScoredTerms()) {
                Term term = scoredTerm.getTerm();

                Term persistedTerm = persistence
                        .getOrCreateTerm(term.getCategory(), term.getValue());
                scoredTerm.setTerm(persistedTerm);
            }
        }
    }
}
