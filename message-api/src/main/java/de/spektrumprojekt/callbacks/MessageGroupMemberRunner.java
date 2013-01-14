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

import de.spektrumprojekt.datamodel.message.MessageGroup;

/**
 * Runs a callback for each user that is member of the message group
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            The type of context that will be passed to the user callback
 */
public interface MessageGroupMemberRunner<T extends Object> {

    /**
     * For every member in the message group run the callback
     * 
     * @param context
     *            context to be passed
     * @param messageGroup
     *            the message group
     * @param callback
     *            the callback
     */
    public void runForMessageGroup(T context, MessageGroup messageGroup, UserCallback<T> callback);
}
