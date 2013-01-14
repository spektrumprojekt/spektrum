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

package de.spektrumprojekt.callbacks;

import java.util.Collection;

import de.spektrumprojekt.datamodel.message.MessageGroup;

/**
 * Simple callback runner that just calls the callback with the user ids set in the constructor
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 * @param <T>
 */
public class SimpleMessageGroupMemberRunner<T extends Object> implements
        MessageGroupMemberRunner<T> {

    private final Collection<String> userGlobalIds;

    /**
     * 
     * @param userGlobalIds
     *            the ids it will use for the callback
     */
    public SimpleMessageGroupMemberRunner(Collection<String> userGlobalIds) {
        if (userGlobalIds == null) {
            throw new IllegalArgumentException("userGlobalIds cannot be null");
        }
        if (userGlobalIds.contains(null)) {
            throw new IllegalArgumentException("userGlobalIds cannot contain a null value");
        }
        this.userGlobalIds = userGlobalIds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runForMessageGroup(T context, MessageGroup messageGroup,
            UserCallback<T> callback) {

        for (String user : userGlobalIds) {
            callback.run(context, user);
        }
    }

}
