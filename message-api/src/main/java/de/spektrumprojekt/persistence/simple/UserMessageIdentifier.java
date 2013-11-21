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

package de.spektrumprojekt.persistence.simple;

import de.spektrumprojekt.datamodel.message.UserMessageScore;

/**
 * Identifier for user and message id, used within the simple persistence
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UserMessageIdentifier {

    private final String userGlobalId;

    private final String messageGlobalId;

    public UserMessageIdentifier(UserMessageScore messageRank) {
        this(messageRank.getUserGlobalId(), messageRank.getMessageGlobalId());
    }

    public UserMessageIdentifier(String userGlobalId,
            String messageGlobalId) {
        if (userGlobalId == null) {
            throw new IllegalArgumentException("userGlobalId cannot be null.");
        }
        if (messageGlobalId == null) {
            throw new IllegalArgumentException("messageGlobalId cannot be null.");
        }
        this.userGlobalId = userGlobalId;
        this.messageGlobalId = messageGlobalId;

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UserMessageIdentifier other = (UserMessageIdentifier) obj;
        if (messageGlobalId == null) {
            if (other.messageGlobalId != null) {
                return false;
            }
        } else if (!messageGlobalId.equals(other.messageGlobalId)) {
            return false;
        }
        if (userGlobalId == null) {
            if (other.userGlobalId != null) {
                return false;
            }
        } else if (!userGlobalId.equals(other.userGlobalId)) {
            return false;
        }
        return true;
    }

    public String getMessageGlobalId() {
        return messageGlobalId;
    }

    public String getUserGlobalId() {
        return userGlobalId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (messageGlobalId == null ? 0 : messageGlobalId.hashCode());
        result = prime * result + (userGlobalId == null ? 0 : userGlobalId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "UserMessageIdentifier [userGlobalId=" + userGlobalId + ", messageGlobalId="
                + messageGlobalId + "]";
    }

}
