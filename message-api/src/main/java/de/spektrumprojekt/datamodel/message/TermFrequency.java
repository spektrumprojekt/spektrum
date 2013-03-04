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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.SimpleType;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;
import de.spektrumprojekt.datamodel.observation.ObservationType;

/**
 * An object of this class stores some observation made about an user. There is a type of
 * observation {@link ObservationType} defining what kind of observation was made. Based on the type
 * the {@link #observation} itself should store the information, e.g. the id of the message that was
 * stored or liked.
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public class TermFrequency extends Identifiable {

    public final static String SINGLE_GLOBAL_ID = "termFrequency";

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String messageGroupMessageCountsJson;
    private transient Map<String, Integer> messageGroupMessageCounts;

    private long allTermCount;
    private long uniqueTermCount;
    private long messageCount;

    private static final transient ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final transient MapType MESSAGE_GROUP_MESSAGE_COUNTS_MAP_TYPE = MapType
            .construct(
                    HashMap.class,
                    SimpleType.construct(String.class), SimpleType.construct(Integer.class));

    public TermFrequency() {
        super(SINGLE_GLOBAL_ID);
    }

    public long getAllTermCount() {
        return allTermCount;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public Map<String, Integer> getMessageGroupMessageCounts() {

        return messageGroupMessageCounts;
    }

    public long getUniqueTermCount() {
        return uniqueTermCount;
    }

    public void incrementMessageGroupMessageCount(String messageGroupGlobalId) {

        Integer val = this.messageGroupMessageCounts.get(messageGroupGlobalId);
        if (val == null) {
            val = 1;
        } else {
            val = val + 1;
        }
        this.messageGroupMessageCounts.put(messageGroupGlobalId, val);

    }

    public void init() {

        if (messageGroupMessageCountsJson == null) {
            messageGroupMessageCounts = new HashMap<String, Integer>();
        } else {
            try {
                messageGroupMessageCounts = OBJECT_MAPPER.readValue(
                        this.messageGroupMessageCountsJson,
                        MESSAGE_GROUP_MESSAGE_COUNTS_MAP_TYPE);
            } catch (JsonParseException e) {
                throw new RuntimeException("Error deserializing map", e);
            } catch (JsonMappingException e) {
                throw new RuntimeException("Error deserializing map", e);
            } catch (IOException e) {
                throw new RuntimeException("Error deserializing map", e);
            }
        }
    }

    public void prepareForStore() {
        try {
            this.messageGroupMessageCountsJson = OBJECT_MAPPER.writeValueAsString(
                    this.messageGroupMessageCounts);
        } catch (JsonGenerationException e) {
            throw new RuntimeException("Error serializing map", e);
        } catch (JsonMappingException e) {
            throw new RuntimeException("Error serializing map", e);
        } catch (IOException e) {
            throw new RuntimeException("Error serializing map", e);
        }
    }

    public void resetMessageGroupMessageCounts() {
        this.messageGroupMessageCounts.clear();
    }

    public void setAllTermCount(long allTermCount) {
        this.allTermCount = allTermCount;
    }

    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    public void setMessageGroupMessageCount(String messageGroupGlobalId, Integer count) {

        if (count == 0) {
            this.messageGroupMessageCounts.remove(messageGroupGlobalId);
        } else {
            this.messageGroupMessageCounts.put(messageGroupGlobalId, count);
        }
    }

    public void setUniqueTermCount(long uniqueTermCount) {
        this.uniqueTermCount = uniqueTermCount;
    }
}