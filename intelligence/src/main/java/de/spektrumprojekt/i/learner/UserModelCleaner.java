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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.persistence.simple.SimplePersistence;
import de.spektrumprojekt.persistence.simple.UserModelHolder;

public class UserModelCleaner implements ConfigurationDescriptable {

    private SimplePersistence persistence;

    public UserModelCleaner(SimplePersistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    // TODO synchronized with the learning some how
    public void run() {
        // this is simple persistence and evaluation specific
        this.persistence.clearMessageRanks();
        this.persistence.resetTermsPrepare();

        Set<Term> termsStillNeeded = new HashSet<Term>();
        for (Entry<String, Map<User, UserModelHolder>> entriesOfUserModelTypes : persistence
                .getUserModelByTypeHolders().entrySet()) {

            for (UserModelHolder userModel : entriesOfUserModelTypes.getValue().values()) {
                Collection<Term> delete = new HashSet<Term>();
                for (Entry<Term, UserModelEntry> entry : userModel.getUserModelEntries().entrySet()) {
                    entry.getValue().consolidate();
                    if (entry.getValue().getTimeBinEntries() == null
                            || entry.getValue().getTimeBinEntries().size() == 0) {
                        delete.add(entry.getKey());
                    } else {
                        termsStillNeeded.add(entry.getKey());
                    }
                }
                for (Term t : delete) {
                    // TODO this should be done using the persistence
                    persistence.removeUserModelEntry(userModel.getUserModel(), userModel
                            .getUserModelEntries().get(t));
                }
            }
        }
        this.persistence.resetTerms(termsStillNeeded);

    }

    @Override
    public String toString() {
        return getConfigurationDescription();
    }
}
