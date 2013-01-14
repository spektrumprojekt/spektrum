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

package de.spektrumprojekt.aggregator.duplicate.hashduplicate;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ManyToMany;

import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.aggregator.duplicate.DuplicateDetection;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.persistence.Persistence;

/**
 * duplicate detection by comparing the hash of a message
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class HashDuplicationDetection implements DuplicateDetection {

    private final Persistence persistence;

    /**
     * minimum number of message hashes per Subscription
     */
    private final int minValue;

    /**
     * maximum number of message hashes per Subscription
     */
    private final int maxValue;

    @ManyToMany
    private final Set<SubscriptionMessageHashes> subsctiptionsWithHashes = new HashSet<SubscriptionMessageHashes>();

    /**
     * constructor
     * 
     * @param configuration
     *            AggregatorConfiguration
     */
    public HashDuplicationDetection(AggregatorConfiguration configuration, Persistence persistence) {
        maxValue = configuration.getMaxHashes();
        minValue = configuration.getMinHashes();
        this.persistence = persistence;
    }

    /**
     * searches the hashes for the subscription with the given id
     * 
     * @param subscriptionGlobalId
     *            global id of the subscription
     * @return hashes
     */
    private SubscriptionMessageHashes getHashes(String subscriptionGlobalId) {
        for (SubscriptionMessageHashes subscriptionHashes : subsctiptionsWithHashes) {
            if (subscriptionHashes.getSubscriptionGlobalId().equals(subscriptionGlobalId)) {
                return subscriptionHashes;
            }
        }
        SubscriptionMessageHashes hashes = new SubscriptionMessageHashes(persistence,
                subscriptionGlobalId, minValue, maxValue);
        subsctiptionsWithHashes.add(hashes);
        return hashes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDuplicate(Message message) {
        return getHashes(message.getSubscriptionGlobalId()).isDuplicate(message);
    }

}
