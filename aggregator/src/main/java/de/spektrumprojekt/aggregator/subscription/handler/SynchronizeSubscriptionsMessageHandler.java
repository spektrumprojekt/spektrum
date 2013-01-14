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

package de.spektrumprojekt.aggregator.subscription.handler;

import java.util.List;

import de.spektrumprojekt.aggregator.subscription.SubscriptionManager;
import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.MessageHandler;
import de.spektrumprojekt.communication.transfer.SynchronizeSubscriptionsMessage;
import de.spektrumprojekt.datamodel.subscription.Subscription;

public class SynchronizeSubscriptionsMessageHandler implements
        MessageHandler<SynchronizeSubscriptionsMessage> {

    private final SubscriptionManager subscriptionManager;

    public SynchronizeSubscriptionsMessageHandler(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void deliverMessage(SynchronizeSubscriptionsMessage message) throws Exception {
        List<Subscription> subscriptions = message.getSubscriptions();
        if (subscriptions == null) {
            throw new IllegalArgumentException("subscriptions not set for message " + message);
        }
        subscriptionManager.synchronizeSubscriptions(subscriptions);
    }

    @Override
    public Class<SynchronizeSubscriptionsMessage> getMessageClass() {
        return SynchronizeSubscriptionsMessage.class;
    }

    @Override
    public boolean supports(CommunicationMessage message) {
        return message instanceof SynchronizeSubscriptionsMessage;
    }
}
