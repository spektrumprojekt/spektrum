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

package de.spektrumprojekt.datamodel.message;

import java.util.Arrays;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;

/**
 * The relation of the message
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public class MessageRelation extends Identifiable {

    /**
     * Enum definining the type of relation
     * 
     * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
     * 
     */
    public enum MessageRelationType {
        DISCUSSION,
        RELATION
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private MessageRelationType messageRelationType;

    private String[] releatedMessageGlobalIds;

    /**
     * for orm
     */
    protected MessageRelation() {
        // for orm
    }

    /**
     * 
     */
    public MessageRelation(MessageRelationType messageRelationType, String globalId,
            String[] globalMessageIds) {
        super(globalId);
        this.messageRelationType = messageRelationType;
        this.releatedMessageGlobalIds = globalMessageIds;
    }

    /**
     * 
     */
    public MessageRelation(MessageRelationType messageRelationType,
            String[] globalMessageIds) {
        super();
        this.messageRelationType = messageRelationType;
        this.releatedMessageGlobalIds = globalMessageIds;
    }

    public MessageRelationType getMessageRelationType() {
        return messageRelationType;
    }

    public String[] getReleatedMessageGlobalIds() {
        return releatedMessageGlobalIds;
    }

    /**
     * Check if the message is related
     * 
     * @param message
     * @return
     */
    public boolean isRelated(Message message) {
        if (releatedMessageGlobalIds == null || releatedMessageGlobalIds.length == 0) {
            return true;
        }
        if (releatedMessageGlobalIds.length == 1) {
            return message.getGlobalId().equals(releatedMessageGlobalIds[0]);
        }
        return false;
    }

    @Override
    public String toString() {
        return "MessageRelation [messageRelationType=" + messageRelationType
                + ", releatedMessageGlobalIds=" + Arrays.toString(releatedMessageGlobalIds)
                + ", getGlobalId()=" + getGlobalId() + ", getId()=" + getId() + "]";
    }

}
