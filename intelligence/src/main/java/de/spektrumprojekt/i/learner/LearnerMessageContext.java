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

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.persistence.Persistence;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class LearnerMessageContext extends MessageFeatureContext {

    private final Observation observation;

    private final Collection<Observation> relatedObservations = new HashSet<Observation>();

    public LearnerMessageContext(Persistence persistence, Observation observation, Message message,
            MessageRelation messageRelation) {
        super(persistence, message, messageRelation);
        if (observation == null) {
            throw new IllegalArgumentException("observation cannot be null.");
        }
        this.observation = observation;

    }

    public Observation getObservation() {
        return observation;
    }

    public Collection<Observation> getRelatedObservations() {
        return relatedObservations;
    }

}
