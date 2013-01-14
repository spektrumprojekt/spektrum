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

import java.util.Collection;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.user.User;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UserModelAdapter {

    /**
     * TODO
     */
    public void process() {

        // find new trend terms

        // try to infer interests for a user
        // 1. find users who have rated the message
        // 2. find common interests with available users
        // 3. adapt the profile of those user

        // TODO find common interests

        // TODO use last comments

        // adapt users
    }

    /**
     * TODO
     * 
     * @param message
     *            the message to process
     * @param users
     *            the users that will need adaption
     */
    public void processMessage(Message message, Collection<User> users) {
        // this message has been new

        // 1st find out new terms for each user

        // if really ne
    }
}
