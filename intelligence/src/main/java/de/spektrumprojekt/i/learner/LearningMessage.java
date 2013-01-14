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

package de.spektrumprojekt.i.learner;

import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRelation;

/**
 * Messages containing an interest
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class LearningMessage implements CommunicationMessage {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Message message;

    private MessageRelation messageRelation;

    private Interest interest;

    private String userToLearnForGlobalId;

    /**
     * For json deserialization
     */
    protected LearningMessage() {
        // for json deserialization
    }

    /**
     * 
     * @param message
     *            the message to learn for
     * @param messageRelation
     *            the message relation, cann be null
     * @param userToLernForGlobalId
     *            the id of the user to learn for
     * @param interest
     *            the interest into the message
     */
    public LearningMessage(Message message, MessageRelation messageRelation,
            String userToLernForGlobalId, Interest interest) {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null!");
        }
        if (userToLernForGlobalId == null) {
            throw new IllegalArgumentException("userToLernForGlobalId cannot be null!");
        }
        if (interest == null) {
            throw new IllegalArgumentException("interest cannot be null!");
        }
        this.message = message;
        this.messageRelation = messageRelation;
        this.userToLearnForGlobalId = userToLernForGlobalId;
        this.interest = interest;
    }

    /**
     * 
     * @param message
     *            the message to learn for
     * @param userToLernForGlobalId
     *            the id of the user to learn for
     * @param interest
     *            the interest into the message
     */
    public LearningMessage(Message message, String userToLernForGlobalId, Interest interest) {
        this(message, null, userToLernForGlobalId, interest);
    }

    /**
     * 
     * @return the interest
     */
    public Interest getInterest() {
        return interest;
    }

    /**
     * 
     * @return the message
     */
    public Message getMessage() {
        return message;
    }

    public MessageRelation getMessageRelation() {
        return messageRelation;
    }

    /**
     * 
     * @return the user to learn for
     */
    public String getUserToLearnForGlobalId() {
        return userToLearnForGlobalId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String retrieveMessageType() {
        return LearningMessage.class.getSimpleName();
    }

    public void setInterest(Interest interest) {
        if (interest == null) {
            throw new IllegalArgumentException("interest cannot be null.");
        }
        this.interest = interest;
    }

    public void setMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null.");
        }
        this.message = message;
    }

    public void setMessageRelation(MessageRelation messageRelation) {
        this.messageRelation = messageRelation;
    }

    public void setUserToLearnForGlobalId(String userToLearnForGlobalId) {
        this.userToLearnForGlobalId = userToLearnForGlobalId;
    }

    public void setUserToLernForGlobalId(String userToLernForGlobalId) {
        if (userToLernForGlobalId == null) {
            throw new IllegalArgumentException("userToLernForGlobalId cannot be null.");
        }
        this.userToLearnForGlobalId = userToLernForGlobalId;
    }

}
