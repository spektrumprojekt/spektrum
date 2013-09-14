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

import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.datamodel.message.Message;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class MessageCommunicationMessage implements CommunicationMessage {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Message message;
    private String[] subscriptionGlobalIds;

    protected MessageCommunicationMessage() {
    }

    /**
     * 
     * @param message
     *            the message
     * @param subscriptionGlobalIds
     *            the subscription ids this message is belonging to
     */
    public MessageCommunicationMessage(Message message, String... subscriptionGlobalIds) {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }
        this.message = message;
        this.subscriptionGlobalIds = subscriptionGlobalIds;
    }

    public Message getMessage() {
        return message;
    }

    public String[] getSubscriptionGlobalIds() {
        return subscriptionGlobalIds;
    }

    @Override
    public String retrieveMessageType() {
        return this.getClass().getSimpleName();
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void setSubscriptionGlobalIds(String[] subscriptionGlobalIds) {
        this.subscriptionGlobalIds = subscriptionGlobalIds;
    }

}
