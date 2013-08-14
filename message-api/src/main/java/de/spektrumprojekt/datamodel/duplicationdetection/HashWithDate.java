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

package de.spektrumprojekt.datamodel.duplicationdetection;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import de.spektrumprojekt.datamodel.identifiable.SpektrumEntity;

@Entity
public class HashWithDate implements SpektrumEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
    private String subscriptionGlobalId;

    @Temporal(value = TemporalType.DATE)
    private Date time;

    @Id
    @Column(length = 1024)
    private String hash;

    /**
     * for persistence
     */
    public HashWithDate() {
    }

    public HashWithDate(String subscriptionGlobalId, Date time, String hash) {
        super();
        this.subscriptionGlobalId = subscriptionGlobalId;
        this.time = time;
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    public String getSubscriptionGlobalId() {
        return subscriptionGlobalId;
    }

    public String getSubscriptionId() {
        return subscriptionGlobalId;
    }

    public Date getTime() {
        return time;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setSubscriptionGlobalId(String subscriptionGlobalId) {
        this.subscriptionGlobalId = subscriptionGlobalId;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
