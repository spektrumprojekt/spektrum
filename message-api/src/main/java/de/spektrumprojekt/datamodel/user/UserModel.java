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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;

/**
 * A user model for a user
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public class UserModel extends Identifiable {

    public static final String DEFAULT_USER_MODEL_TYPE = "DEFAULT_USER_MODEL";

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * the user
     */
    @ManyToOne
    private User user;

    private String userModelType;

    protected UserModel() {
        // for jpa only
    }

    public UserModel(User user, String userModelType) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null.");
        }
        if (userModelType == null) {
            throw new IllegalArgumentException("userModelType cannot be null.");
        }
        this.user = user;
        this.userModelType = userModelType;
    }

    /**
     * 
     * @return the user
     */
    public User getUser() {
        return user;
    }

    public String getUserModelType() {
        return userModelType;
    }

    /**
     * 
     * @param user
     *            the user
     */
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "UserModel [user=" + user + ", userModelType=" + userModelType + "]";
    }

}
