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

package de.spektrumprojekt.datamodel.message;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.Validate;
import org.codehaus.jackson.annotate.JsonIgnore;

import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.identifiable.Identifiable;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;

/**
 * <p>
 * The Message.
 * </p>
 * 
 * @author Marius Feldmann
 * @author Philipp Katz
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public class Message extends Identifiable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @OneToMany(cascade = CascadeType.ALL)
    private final Collection<MessagePart> messageParts = new HashSet<MessagePart>();

    @OneToMany(cascade = CascadeType.ALL)
    private final Collection<Property> properties = new HashSet<Property>();

    private MessageType messageType;

    private StatusType statusType;

    private String subscriptionGlobalId;

    // TODO ? author of this message - if known.
    private String authorGlobalId;

    @ManyToOne(optional = true)
    private MessageGroup messageGroup;

    @Temporal(TemporalType.TIMESTAMP)
    private Date publicationDate;

    protected Message() {
    }

    public Message(MessageType messageType, StatusType statusType,
            Date publicationDate) {
        this(messageType, statusType, null, publicationDate);
    }

    public Message(MessageType messageType, StatusType statusType, String subscriptionGlobalId,
            Date publicationDate) {
        if (publicationDate == null) {
            throw new IllegalArgumentException("publicationDate cannot be null!");
        }
        if (statusType == null) {
            throw new IllegalArgumentException("statusType cannot be null!");
        }
        if (messageType == null) {
            throw new IllegalArgumentException("messageType cannot be null!");
        }
        this.messageType = messageType;
        this.statusType = statusType;
        this.subscriptionGlobalId = subscriptionGlobalId;
        this.publicationDate = publicationDate;
    }

    public Message(String globalId, MessageType messageType, StatusType statusType,
            String subscriptionGlobalId,
            Date publicationDate) {
        super(globalId);
        if (publicationDate == null) {
            throw new IllegalArgumentException("publicationDate cannot be null!");
        }
        if (statusType == null) {
            throw new IllegalArgumentException("statusType cannot be null!");
        }
        if (messageType == null) {
            throw new IllegalArgumentException("messageType cannot be null!");
        }
        this.messageType = messageType;
        this.statusType = statusType;
        this.subscriptionGlobalId = subscriptionGlobalId;
        this.publicationDate = publicationDate;
    }

    public void addMessagePart(MessagePart messagePart) {
        messageParts.add(messagePart);
    }

    public void addProperty(Property property) {
        Validate.notNull(property, "property cannot be null");
        // TODO how to deal with properties of same key ?
        this.properties.add(property);
    }

    public void clearMessageParts() {
        messageParts.clear();
    }

    public String getAuthorGlobalId() {
        return authorGlobalId;
    }

    public MessageGroup getMessageGroup() {
        return messageGroup;
    }

    public Collection<MessagePart> getMessageParts() {
        return messageParts;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Collection<Property> getProperties() {
        return properties;
    }

    /**
     * Build a map of properties of the available properties. The map is been build upon each call
     * of this method.
     * 
     * @return the map with key as {@link Property#getPropertyKey()} and value as {@link Property}
     */
    @JsonIgnore
    public Map<String, Property> getPropertiesAsMap() {
        Map<String, Property> propertyMap = new HashMap<String, Property>();
        for (Property p : this.properties) {
            propertyMap.put(p.getPropertyKey(), p);
        }
        return propertyMap;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public StatusType getStatusType() {
        return statusType;
    }

    public String getSubscriptionGlobalId() {
        return subscriptionGlobalId;
    }

    public void removeFromMessageParts(MessagePart value) {
        messageParts.remove(value);
    }

    public void setAuthorGlobalId(String authorGlobalId) {
        this.authorGlobalId = authorGlobalId;
    }

    public void setMessageGroup(MessageGroup messageGroup) {
        this.messageGroup = messageGroup;
    }

    @Override
    public String toString() {
        final int maxLen = 5;
        return "Message [messageParts="
                + (messageParts != null ? toString(messageParts, maxLen) : null) + ", properties="
                + (properties != null ? toString(properties, maxLen) : null) + ", messageType="
                + messageType + ", statusType=" + statusType + ", subscriptionGlobalId="
                + subscriptionGlobalId + ", authorGlobalId=" + authorGlobalId + ", messageGroup="
                + messageGroup + ", publicationDate=" + publicationDate + "]";
    }

    private String toString(Collection<?> collection, int maxLen) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(iterator.next());
        }
        builder.append("]");
        return builder.toString();
    }

}
