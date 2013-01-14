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

package de.spektrumprojekt.datamodel.user;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;

/**
 * So far only the users id is used
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
@Entity
@Table(name = "spektrumuser", uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public class User extends Identifiable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * for jpa
     */
    protected User() {
        // nothing to do
    }

    public User(String userGlobalId) {
        super(userGlobalId);
    }
}
