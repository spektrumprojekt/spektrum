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

package de.spektrumprojekt.helper;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;

/**
 * Test the {@link MessageHelper}
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class MessageHelperTest {

    /**
     * Test the mentions (splitting and parsing)
     */
    @Test
    public void testMentionProperty() {

        String[] userGlobalIds = new String[] {
                "id1",
                "id2",
                "id3",
                "id4",
        };

        Property property = MessageHelper.createMentionProperty(userGlobalIds);
        Assert.assertNotNull("Mention Property should not be null", property);

        Message message = new Message(MessageType.CONTENT, StatusType.OK, "subscriptionGlobalId",
                new Date());
        message.addProperty(property);

        for (String userGlobalId : userGlobalIds) {
            Assert.assertTrue(
                    userGlobalId + " must be contained in message property= "
                            + property.getPropertyValue(),
                    MessageHelper.isMentioned(message, userGlobalId));
        }
    }

    /**
     * Test the mentions (splitting and parsing)
     */
    @Test
    public void testUserLikeProperty() {

        String[] userGlobalIds = new String[] {
                "id1",
                "id2",
                "id3",
                "id4",
        };

        Property property = MessageHelper.createUserLikesProperty(userGlobalIds);
        Assert.assertNotNull("User Likes Property should not be null", property);

        Message message = new Message(MessageType.CONTENT, StatusType.OK, "subscriptionGlobalId",
                new Date());
        message.addProperty(property);

        for (String userGlobalId : userGlobalIds) {
            Assert.assertTrue(
                    userGlobalId + " must be contained in message property= "
                            + property.getPropertyValue(),
                    MessageHelper.isLikedByUser(message, userGlobalId));
        }
    }
}
