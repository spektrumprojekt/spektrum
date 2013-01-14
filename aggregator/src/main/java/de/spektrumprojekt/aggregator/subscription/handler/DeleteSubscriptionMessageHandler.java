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



import de.spektrumprojekt.aggregator.subscription.SubscriptionManager;
import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.MessageHandler;
import de.spektrumprojekt.communication.transfer.DeleteSubscriptionMessage;

public class DeleteSubscriptionMessageHandler implements
        MessageHandler<DeleteSubscriptionMessage> {

    private SubscriptionManager subscriptionManager;

    public DeleteSubscriptionMessageHandler(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void deliverMessage(DeleteSubscriptionMessage message) throws Exception {

        String subscriptionGlobalId = message.getSubscriptionGlobalId();

        if (subscriptionGlobalId == null) {
            throw new IllegalArgumentException("subscriptionGlobalId not set for message "
                    + message);
        }

        subscriptionManager.unsubscribe(subscriptionGlobalId);
    }

    @Override
    public Class<DeleteSubscriptionMessage> getMessageClass() {
        return DeleteSubscriptionMessage.class;
    }

    @Override
    public boolean supports(CommunicationMessage message) {
        return message instanceof DeleteSubscriptionMessage;
    }
}
