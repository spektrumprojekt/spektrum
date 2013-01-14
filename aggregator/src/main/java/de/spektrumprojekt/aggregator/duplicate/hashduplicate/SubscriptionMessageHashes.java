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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.spektrumprojekt.aggregator.duplicate.DuplicateDetection;
import de.spektrumprojekt.datamodel.duplicationdetection.HashWithDate;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.persistence.Persistence;

/**
 * saves the hashes of messages for a subscription, if the maxValue is reached the hashes are
 * cleaned and only the last ones are kept (minValue).
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class SubscriptionMessageHashes implements DuplicateDetection {

    private final String subscriptionGlobalId;

    private final int maxValue;

    private final int minValue;

    private final Persistence persistence;

    private final List<HashWithDate> hashes = new LinkedList<HashWithDate>();

    /**
     * Constructor
     * 
     * @param persistence
     *            persistence
     * @param subscriptionGlobalId
     *            subscriptionGlobalId of the subscription
     * @param minValue
     *            minValue
     * @param maxValue
     *            maxValue
     */
    public SubscriptionMessageHashes(Persistence persistence,
            String subscriptionGlobalId, int minValue, int maxValue) {
        this.subscriptionGlobalId = subscriptionGlobalId;
        this.persistence = persistence;
        this.maxValue = maxValue;
        this.minValue = minValue;
        loadHashes();
    }

    /**
     * removes unnecessary hashes
     */
    private void cleanup() {
        if (hashes.size() > maxValue) {
            List<HashWithDate> hashesToDelete = new ArrayList<HashWithDate>();

            while (hashes.size() > minValue) {
                HashWithDate hashToDelete = null;
                for (HashWithDate hash : hashes) {
                    if (hashToDelete == null) {
                        hashToDelete = hash;
                    } else {
                        if (hashToDelete.getTime().after(hash.getTime())) {
                            hashToDelete = hash;
                        }
                    }
                }
                hashesToDelete.add(hashToDelete);
                hashes.remove(hashToDelete);
            }
            persistence.deleteHashWithDates(hashesToDelete);
        }
    }

    /**
     * 
     * @return subscriptionGlobalId
     */
    public String getSubscriptionGlobalId() {
        return subscriptionGlobalId;
    }

    @Override
    public boolean isDuplicate(Message message) {
        String messageHash = MessageHashGenerator.generateHashFromMessage(message);
        for (HashWithDate hashWithDate : hashes) {
            if (hashWithDate.getHash()
                    .equals(messageHash)) {
                return true;
            }
        }
        HashWithDate hashWithDate = new HashWithDate(subscriptionGlobalId, new Date(),
                MessageHashGenerator.generateHashFromMessage(message));
        hashWithDate = persistence.saveHashWithDate(hashWithDate);
        hashes.add(hashWithDate);
        cleanup();
        return false;
    }

    /***
     * loads the hashes from the database
     */
    private void loadHashes() {
        hashes.addAll(persistence.getHashsByGlobalSubscriptionId(subscriptionGlobalId));
    }
}
