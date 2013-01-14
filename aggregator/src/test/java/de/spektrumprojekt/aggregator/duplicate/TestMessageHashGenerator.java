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

package de.spektrumprojekt.aggregator.duplicate;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.aggregator.duplicate.hashduplicate.MessageHashGenerator;
import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;


public class TestMessageHashGenerator {

    private final List<Message> messages = new LinkedList<Message>();

    @Before
    public void setup() {
        for (int i = 0; i < 10; i++) {
            Message message = new Message(MessageType.CONTENT, StatusType.OK, "1", new Date());
            MessagePart messagePart = new MessagePart(MimeType.TEXT_HTML, "testnachricht" + i);
            message.addMessagePart(messagePart);
            messages.add(message);
        }
    }

    @Test
    public void testMessageHashes() {
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            for (int j = i + 1; j < messages.size(); j++) {
                Assert.assertFalse(MessageHashGenerator.generateHashFromMessage(message).equals(
                        MessageHashGenerator.generateHashFromMessage(messages.get(j))));
            }
        }
    }
}
