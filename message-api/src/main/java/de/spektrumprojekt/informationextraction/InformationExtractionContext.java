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

package de.spektrumprojekt.informationextraction;


import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.persistence.Persistence;

public class InformationExtractionContext {

    private final Message message;
    private final MessagePart messagePart;

    private String cleanText;

    private final Persistence persistence;

    public InformationExtractionContext(Persistence persistence, Message message,
            MessagePart messagePart) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null.");
        }
        if (messagePart == null) {
            throw new IllegalArgumentException("messagePart cannot be null.");
        }
        this.persistence = persistence;
        this.message = message;
        this.messagePart = messagePart;
    }

    public String getCleanText() {
        return cleanText;
    }

    public Message getMessage() {
        return message;
    }

    public MessagePart getMessagePart() {
        return messagePart;
    }

    public Persistence getPersistence() {
        return persistence;
    }

    public void setCleanText(String cleanText) {
        this.cleanText = cleanText;
    }

}
