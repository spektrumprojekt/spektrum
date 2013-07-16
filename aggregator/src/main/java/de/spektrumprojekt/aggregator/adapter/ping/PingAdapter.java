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

package de.spektrumprojekt.aggregator.adapter.ping;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.spektrumprojekt.aggregator.adapter.AdapterException;
import de.spektrumprojekt.aggregator.adapter.BasePollingAdapter;
import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.subscription.SubscriptionStatus;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;

/**
 * 
 * 
 * @author Philipp Katz
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
public final class PingAdapter extends BasePollingAdapter {

    public static final String SOURCE_TYPE = "ping";

    public PingAdapter(AggregatorChain aggregatorChain,
            AggregatorConfiguration aggregatorConfiguration) {
        super(aggregatorChain, aggregatorConfiguration, 10, 1);
    }

    @Override
    public String getSourceType() {
        return SOURCE_TYPE;
    }

    @Override
    public List<Message> poll(SubscriptionStatus subscription) throws AdapterException {
        Message message = new Message(MessageType.CONTENT, StatusType.OK,
                subscription.getGlobalId(), new Date());
        MessagePart part = new MessagePart(MimeType.TEXT_PLAIN, getSourceType());
        message.addMessagePart(part);

        return Collections.singletonList(message);
    }

}
