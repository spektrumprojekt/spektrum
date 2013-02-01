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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;

/**
 * Helper for spektrum/communote specific message handliong
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * @author Philipp Katz
 */
public final class MessageHelper {

    /**
     * Property for the mentions. Contains a comma separated list of user global ids
     * 
     */
    private static final String PROPERTY_KEY_MENTIONS = "mentions";

    private static final char MENTION_SEPERATOR_CHAR = ',';
    private static final String MENTION_SEPERATOR_STR = String.valueOf(MENTION_SEPERATOR_CHAR);

    /**
     * Create the property defining the mentions, the value is just a comma seperation of the ids.
     * 
     * @param userGlobalIds
     *            the ids of the users
     * @return the propert with a value.
     */
    public static Property createMentionProperty(Iterable<String> userGlobalIds) {
        String value = StringUtils.join(userGlobalIds, MENTION_SEPERATOR_CHAR);
        Property property = new Property(PROPERTY_KEY_MENTIONS, value);
        return property;
    }

    /**
     * Create the property defining the mentions, the value is just a comma seperation of the ids.
     * 
     * @param userGlobalIds
     *            the ids of the users
     * @return the propert with a value.
     */
    public static Property createMentionProperty(String[] userGlobalIds) {
        String value = StringUtils.join(userGlobalIds, MENTION_SEPERATOR_CHAR);
        Property property = new Property(PROPERTY_KEY_MENTIONS, value);
        return property;
    }

    /**
     * 
     * @param message
     *            the message
     * @return all terms of all message parts
     */
    public static Collection<Term> getAllTerms(Message message) {
        Collection<Term> terms = new HashSet<Term>();
        for (MessagePart part : message.getMessageParts()) {
            for (ScoredTerm st : part.getScoredTerms()) {
                terms.add(st.getTerm());
            }
        }
        return terms;
    }

    public static Collection<String> getMentions(Message message) {
        Map<String, Property> properties = message.getPropertiesAsMap();
        Property mentionsProperty = properties.get(PROPERTY_KEY_MENTIONS);
        Collection<String> mentions = null;
        if (mentionsProperty != null && mentionsProperty.getPropertyValue() != null) {
            String[] mentionsStr = mentionsProperty.getPropertyValue().split(
                    new String(MENTION_SEPERATOR_STR));
            mentions = new HashSet<String>(Arrays.asList(mentionsStr));
        }
        if (mentions == null) {
            mentions = Collections.emptySet();
        }
        return mentions;
    }

    /**
     * Check for the mention property in the message, and if it exists, check if the given user is
     * included.
     * 
     * @param message
     *            the message o take the properties from
     * @param userGlobalId
     *            the id to check
     * @return true if the user is mentioned
     */
    public static boolean isMentioned(Message message, String userGlobalId) {
        Collection<String> mentionsStr = getMentions(message);
        for (String mention : mentionsStr) {
            if (userGlobalId.equals(mention)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Private constructor
     */
    private MessageHelper() {
        // i am only a helper class :(
    }

    /**
     * <p>
     * Retrieve the title property of a message ({@link Property#PROPERTY_KEY_TITLE}).
     * </p>
     * 
     * @param message
     * @return
     */
    public static String getTitle(Message message) {
        Validate.notNull(message, "message must not be null");

        Property property = message.getPropertiesAsMap().get(Property.PROPERTY_KEY_TITLE);
        if (property == null) {
            return null;
        }
        return property.getPropertyValue();
    }

}
