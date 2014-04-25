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

package de.spektrumprojekt.communication.transfer;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRelation;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class ScorerCommunicationMessage extends MessageCommunicationMessage {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private MessageRelation messageRelation;

    private boolean noRankingOnlyLearning;

    private String[] userGlobalIdsToScoreFor;

    /**
     * // for json deserialization
     */
    protected ScorerCommunicationMessage() {
        // for json deserialization
    }

    public ScorerCommunicationMessage(Message message) {
        super(message);
    }

    public MessageRelation getMessageRelation() {
        return messageRelation;
    }

    public String[] getUserGlobalIdsToScoreFor() {
        return userGlobalIdsToScoreFor;
    }

    public boolean isNoScoringOnlyLearning() {
        return noRankingOnlyLearning;
    }

    public void setMessageRelation(MessageRelation messageRelation) {
        this.messageRelation = messageRelation;
    }

    public void setNoScoringOnlyLearning(boolean noRankingOnlyLearning) {
        this.noRankingOnlyLearning = noRankingOnlyLearning;
    }

    public void setUserGlobalIdsToScoreFor(String[] userGlobalIdsToScoreFor) {
        this.userGlobalIdsToScoreFor = userGlobalIdsToScoreFor;
    }

}
