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

import java.util.Date;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.observation.Interest;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;

/**
 * The strategy defines how to integrate an observed interest into the user model
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public interface UserModelEntryIntegrationStrategy extends ConfigurationDescriptable {

    /**
     * Create a new user model entry. New means the user model does not contain it.
     * 
     * @param userModel
     *            the user model the new entry will be assigned to
     * @param interest
     *            the interest
     * @param scoredTerm
     *            the scored term it relates to (e.g. of the message). do not use if for the user
     *            model!
     * @param oberservationDate
     *            TODO
     * @return the newly created user model entry
     */
    public UserModelEntry createNew(UserModel userModel, Interest interest, ScoredTerm scoredTerm,
            Date oberservationDate);

    /**
     * Integrate the term into the user model
     * 
     * @param entry
     *            the entry to use
     * @param interest
     *            the interest observed
     * @param scoredTerm
     *            the term it relates to
     * @param oberservationDate
     *            TODO
     * @return true if the user model entry could be removed (because its empty)
     */
    public boolean integrate(UserModelEntry entry, Interest interest, ScoredTerm scoredTerm,
            Date oberservationDate);

}
