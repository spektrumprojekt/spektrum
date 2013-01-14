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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;

/**
 * Used within the simple persistence to hold the user models with there entries.
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UserModelHolder implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final UserModel userModel;
    private final Map<Term, UserModelEntry> userModelEntries = new HashMap<Term, UserModelEntry>();

    public UserModelHolder(UserModel userModel) {
        if (userModel == null) {
            throw new IllegalArgumentException("userModel cannot be null!");
        }
        this.userModel = userModel;
    }

    public void addUserModelEntry(UserModelEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry cannot be null!");
        }
        if (entry.getScoredTerm() == null) {
            throw new IllegalArgumentException("entry.scoredTerm cannot be null!");
        }
        if (entry.getScoredTerm().getTerm() == null) {
            throw new IllegalArgumentException("entry.scoredTerm.term cannot be null!");
        }
        if (entry.getScoredTerm().getTerm().getId() == null) {
            throw new IllegalArgumentException("entry.scoredTerm.term.id cannot be null!");
        }
        this.userModelEntries.put(entry.getScoredTerm().getTerm(), entry);
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public Map<Term, UserModelEntry> getUserModelEntries() {
        return userModelEntries;
    }

    public UserModelEntry getUserModelEntry(Term term) {
        return userModelEntries.get(term);
    }
}
