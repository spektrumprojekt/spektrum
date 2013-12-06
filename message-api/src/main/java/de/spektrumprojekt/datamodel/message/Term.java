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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.persistence.annotations.Index;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = "globalId"),
        @UniqueConstraint(columnNames = "value") })
public class Term extends Identifiable {

    public enum TermCategory {
        TERM, KEYPHRASE
    }

    public static final String TERM_MESSAGE_GROUP_ID_SEPERATOR = "#";

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Split the name by the message group. if no message group is used the id will be null
     * 
     * @param name
     * @return left: id of the message group, right: name without message group
     */
    public static Pair<Long, String> extractMessageGroupOfName(String name) {
        int indexOfSplit = name.indexOf(TERM_MESSAGE_GROUP_ID_SEPERATOR);

        if (indexOfSplit <= 0) {
            return new ImmutablePair<Long, String>(null, name);
        }
        String id = name.substring(0, indexOfSplit);
        String n = name.substring(indexOfSplit + 1);
        return new ImmutablePair<Long, String>(Long.parseLong(id), n);

    }

    /**
     * Get the term value with the message group id as prefix
     * 
     * @param mg
     *            the mg, if null the given value is returned unchanged
     * @param value
     *            the term value to use
     * @return the term value in the form of <code>12#term</code> for a message group with id '12'
     *         and value 'term'
     */
    public static String getMessageGroupSpecificTermValue(MessageGroup mg, String value) {
        return mg == null ? value : mg.getId() + TERM_MESSAGE_GROUP_ID_SEPERATOR + value;
    }

    @Index
    private String value;

    private TermCategory category;

    @Column(name = "termcount")
    private int count;

    private transient Long messageGroupId;

    protected Term() {
        // constructor only for ORM.
    }

    public Term(TermCategory category, String value) {
        this.value = value;
        this.category = category;

        // TODO better define message group id or relation per term
        Pair<Long, String> mgNamePair = extractMessageGroupOfName(value);

        this.messageGroupId = mgNamePair.getLeft();
    }

    public TermCategory getCategory() {
        return category;
    }

    public int getCount() {
        return count;
    }

    public Long getMessageGroupId() {
        return messageGroupId;
    }

    public String getValue() {
        return value;
    }

    @PostLoad
    private void postLoad() {
        this.messageGroupId = extractMessageGroupOfName(value).getLeft();
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Term [value=");
        builder.append(value);
        builder.append(", category=");
        builder.append(category);
        builder.append(", count=");
        builder.append(count);
        builder.append(", getGlobalId()=");
        builder.append(getGlobalId());
        builder.append(", getId()=");
        builder.append(getId());
        builder.append("]");
        return builder.toString();
    }

}
