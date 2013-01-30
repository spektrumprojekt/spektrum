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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = "globalId"),
        @UniqueConstraint(columnNames = "value") })
public class Term extends Identifiable {

    public enum TermCategory {
        // TODO or an entity for extensibility ?
        TERM, KEYPHRASE
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String value;
    private TermCategory category;
    @Column(name = "TERMCOUNT")
    private transient int count;

    protected Term() {
        // constructor only for ORM.
    }

    public Term(TermCategory category, String value) {
        this.value = value;
        this.category = category;
    }

    public TermCategory getCategory() {
        return category;
    }

    public int getCount() {
        return count;
    }

    public String getValue() {
        return value;
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
