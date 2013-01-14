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

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.persistence.Persistence;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class LearnerMessageContext extends MessageFeatureContext {

    private final String userToLearnForGlobalId;
    private final Interest interest;

    public LearnerMessageContext(Persistence persistence, Message message,
            MessageRelation relation, String userToLearnForGlobalId, Interest interest) {
        super(persistence, message, relation);
        this.userToLearnForGlobalId = userToLearnForGlobalId;
        this.interest = interest;
    }

    public Interest getInterest() {
        return interest;
    }

    public String getUserToLearnForGlobalId() {
        return userToLearnForGlobalId;
    }
}
