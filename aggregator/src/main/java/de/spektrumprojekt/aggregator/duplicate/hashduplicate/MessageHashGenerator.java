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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.spektrumprojekt.aggregator.adapter.rss.FeedAdapter;
import de.spektrumprojekt.aggregator.adapter.twitter.TwitterAdapter;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;

/**
 * creates message hashes
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class MessageHashGenerator {

    private static final String[] idProperties = { FeedAdapter.MESSAGE_PROPERTY_ID,
            TwitterAdapter.STATUS_ID };

    /**
     * generates the hash for a message
     * 
     * @param message
     *            message
     * @return hash
     */
    public static String generateHashFromMessage(Message message) {
        StringBuilder sb = new StringBuilder();
        for (Property property : message.getProperties()) {
            for (String idProperty : idProperties) {
                if (property.getPropertyKey().equals(idProperty)) {
                    sb.append(idProperty);
                    sb.append(":");
                    sb.append(property.getPropertyValue());
                    return sb.toString();
                }
            }
        }
        sb.append("hash:");
        for (MessagePart messagePart : message.getMessageParts()) {
            sb.append(messagePart.getContent());
        }
        MessageDigest md;
        try {
            byte[] bytesOfMessage = sb.toString().getBytes("UTF-8");
            md = MessageDigest.getInstance("MD5");
            return new String(md.digest(bytesOfMessage));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Private constructor
     */
    private MessageHashGenerator() {
        // i am only a helper class :(
    }
}
