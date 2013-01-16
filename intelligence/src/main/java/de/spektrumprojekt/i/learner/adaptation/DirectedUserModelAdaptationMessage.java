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

package de.spektrumprojekt.i.learner.adaptation;

import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.datamodel.message.Term;

public class DirectedUserModelAdaptationMessage implements CommunicationMessage {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String messageId;
    private String messageGroupGlobalId;

    private String userGlobalId;
    private Term[] termsToAdapt;

    /**
     * for json deserialization
     */
    protected DirectedUserModelAdaptationMessage() {
        // for json deserialization
    }

    public DirectedUserModelAdaptationMessage(String userGlobalId, String messageId,
            String messageGroupGlobalId,
            Term[] termsToAdapt) {
        if (userGlobalId == null) {
            throw new IllegalArgumentException("userGlobalId cannot be null.");
        }
        if (messageGroupGlobalId == null) {
            throw new IllegalArgumentException("messageGroupGlobalId cannot be null.");
        }
        if (messageId == null) {
            throw new IllegalArgumentException("messageId cannot be null.");
        }
        if (termsToAdapt == null || termsToAdapt.length == 0) {
            throw new IllegalArgumentException("termsToAdapt cannot be null or empty.");
        }
        this.userGlobalId = userGlobalId;
        this.messageId = messageId;
        this.messageGroupGlobalId = messageGroupGlobalId;
        this.termsToAdapt = termsToAdapt;
    }

    public String getMessageGroupGlobalId() {
        return messageGroupGlobalId;
    }

    public String getMessageId() {
        return messageId;
    }

    public Term[] getTermsToAdapt() {
        return termsToAdapt;
    }

    public String getUserGlobalId() {
        return userGlobalId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String retrieveMessageType() {
        return this.getClass().getSimpleName();
    }

}
