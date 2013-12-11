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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
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

    private static final char MENTION_SEPERATOR_CHAR = ',';
    private static final String MENTION_SEPERATOR_STR = String.valueOf(MENTION_SEPERATOR_CHAR);
    /**
     * Property for the mentions. Contains a comma separated list of user global ids
     * 
     */
    private static final String PROPERTY_KEY_MENTIONS = "mentions";

    private static final char TAG_SEPERATOR_CHAR = ',';
    private static final String TAG_SEPERATOR_STR = String.valueOf(MENTION_SEPERATOR_CHAR);
    /**
     * Property for the mentions. Contains a comma separated list of user global ids
     * 
     */
    private static final String PROPERTY_KEY_TAGS = "tags";

    private static final char USERS_LIKE_SEPERATOR_CHAR = ',';
    private static final String USERS_LIKE_SEPERATOR_CHAR_STR = String
            .valueOf(USERS_LIKE_SEPERATOR_CHAR);
    private static final String PROPERTY_KEY_USERS_LIKE = "likes";

    private static final String PROPERTY_KEY_PARENT_MESSAGE = "parent";

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

    public static Property createParentMessageProperty(String messageGlobalId) {
        Property property = new Property(PROPERTY_KEY_PARENT_MESSAGE, messageGlobalId);
        return property;
    }

    public static Property createTagProperty(Iterable<String> tags) {
        String value = StringUtils.join(tags, TAG_SEPERATOR_CHAR);
        Property property = new Property(PROPERTY_KEY_TAGS, value);
        return property;
    }

    public static Property createUserLikesProperty(Iterable<String> userGlobalIds) {
        String value = StringUtils.join(userGlobalIds, USERS_LIKE_SEPERATOR_CHAR);
        Property property = new Property(PROPERTY_KEY_USERS_LIKE, value);
        return property;
    }

    public static Property createUserLikesProperty(String[] userGlobalIds) {
        String value = StringUtils.join(userGlobalIds, USERS_LIKE_SEPERATOR_CHAR);
        Property property = new Property(PROPERTY_KEY_USERS_LIKE, value);
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

    public static String getAuthor(Message message) {
        Property property = message.getProperty(Property.PROPERTY_KEY_AUTHOR_NAME);
        if (property != null) {
            return property.getPropertyValue();
        }
        property = message.getProperty(Property.PROPERTY_KEY_DC_CREATOR);
        if (property != null) {
            return property.getPropertyValue();
        }
        return null;
    }

    public static String getLink(Message message) {
        Property property = message.getProperty(Property.PROPERTY_KEY_LINK);
        return property != null ? property.getPropertyValue() : null;
    }

    private static Collection<String> getListProperty(Message message, String propertyKey,
            String seperationString) {
        Map<String, Property> properties = message.getPropertiesAsMap();
        Property property = properties.get(propertyKey);
        Collection<String> splittedPropertyValues = null;
        if (property != null && property.getPropertyValue() != null) {
            String[] propertyValues = property.getPropertyValue().split(
                    seperationString);
            splittedPropertyValues = new HashSet<String>(Arrays.asList(propertyValues));
        }
        if (splittedPropertyValues == null) {
            splittedPropertyValues = Collections.emptySet();
        }
        return splittedPropertyValues;

    }

    public static Collection<String> getMentions(Message message) {
        return getListProperty(message, PROPERTY_KEY_MENTIONS, MENTION_SEPERATOR_STR);
    }

    public static Property getParentMessage(Message message) {
        Map<String, Property> properties = message.getPropertiesAsMap();
        Property property = properties.get(PROPERTY_KEY_PARENT_MESSAGE);
        return property;
    }

    public static Collection<String> getTags(Message message) {
        return getListProperty(message, PROPERTY_KEY_TAGS, TAG_SEPERATOR_STR);
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

    public static Collection<String> getUserLikes(Message message) {
        return getListProperty(message, PROPERTY_KEY_USERS_LIKE, USERS_LIKE_SEPERATOR_CHAR_STR);
    }

    public static boolean isLikedByUser(Message message, String userGlobalId) {
        Collection<String> likes = getUserLikes(message);
        return likes.contains(userGlobalId);
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
        Collection<String> mentions = getMentions(message);
        return mentions.contains(userGlobalId);
    }

    public static Map<MessageGroup, Collection<Message>> splitByMessageGroup(
            Collection<Message> messages) {
        Map<MessageGroup, Collection<Message>> mg2messages = new HashMap<MessageGroup, Collection<Message>>();

        for (Message message : messages) {
            MessageGroup mg = message.getMessageGroup();
            Collection<Message> mgMessages = mg2messages.get(mg);
            if (mgMessages == null) {
                mgMessages = new HashSet<Message>();
                mg2messages.put(mg, mgMessages);
            }
            mgMessages.add(message);
        }
        return mg2messages;
    }

    /**
     * Private constructor
     */
    private MessageHelper() {
        // i am only a helper class :(
    }

}
