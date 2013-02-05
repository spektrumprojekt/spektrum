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

import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.observation.Observation;

/**
 * Messages containing an interest
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class LearningMessage implements CommunicationMessage {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Observation observation;

    private MessageRelation messageRelation;

    /**
     * For json deserialization
     */
    protected LearningMessage() {
        // for json deserialization
    }

    /**
     * 
     * @param observation
     *            the observation to learn for
     */
    public LearningMessage(Observation observation) {
        this(observation, null);
    }

    /**
     * 
     * @param observation
     *            the observation to learn for
     * @param messageRelation
     *            the message relation, cann be null
     */
    public LearningMessage(Observation observation, MessageRelation messageRelation) {
        if (observation == null) {
            throw new IllegalArgumentException("observation cannot be null!");
        }
        this.observation = observation;
        this.messageRelation = null;
    }

    public MessageRelation getMessageRelation() {
        return messageRelation;
    }

    public Observation getObservation() {
        return observation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String retrieveMessageType() {
        return LearningMessage.class.getSimpleName();
    }

    public void setMessageRelation(MessageRelation messageRelation) {
        this.messageRelation = messageRelation;
    }

    public void setObservation(Observation observation) {
        this.observation = observation;
    }

}
