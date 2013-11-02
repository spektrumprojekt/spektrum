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

package de.spektrumprojekt.i.datamodel;

import de.spektrumprojekt.i.ranker.chain.features.Feature;

/**
 * A message feature
 * 
 * TODO move to some persistence package (even if it will not be persisted ?!)
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class MessageFeature {

    private Feature featureId;

    private String messageGlobalId;
    private String userGlobalId;

    private float value;

    public MessageFeature(Feature featureId) {
        this.featureId = featureId;
    }

    /**
     * 
     * @return the feature
     */
    public Feature getFeatureId() {
        return featureId;
    }

    /**
     * 
     * @return the message id
     */
    public String getMessageGlobalId() {
        return messageGlobalId;
    }

    /**
     * 
     * @return the user id (null if not user specific)
     */
    public String getUserGlobalId() {
        return userGlobalId;
    }

    /**
     * 
     * @return the value
     */
    public float getValue() {
        return value;
    }

    /**
     * 
     * @return if user specific
     */
    public boolean isUserSpecific() {
        return userGlobalId != null;
    }

    /**
     * 
     * @param featureId
     *            the feature
     */
    public void setFeatureId(Feature featureId) {
        this.featureId = featureId;
    }

    /**
     * 
     * @param messageGlobalId
     *            message id
     */
    public void setMessageGlobalId(String messageGlobalId) {
        this.messageGlobalId = messageGlobalId;
    }

    /**
     * 
     * @param userGlobalId
     *            user global id
     */
    public void setUserGlobalId(String userGlobalId) {
        this.userGlobalId = userGlobalId;
    }

    /**
     * 
     * @param value
     *            the value
     */
    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "MessageFeature [value=" + value + ", featureId=" + featureId + ", messageGlobalId="
                + messageGlobalId + ", userGlobalId=" + userGlobalId + "]";
    }
}
