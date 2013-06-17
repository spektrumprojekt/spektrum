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
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.identifiable.Identifiable;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public class MessagePart extends Identifiable {

    private static final long serialVersionUID = 1L;

    private MimeType mimeType;

    @Lob
    private String content;

    @OneToMany(cascade = CascadeType.ALL)
    private final Collection<ScoredTerm> scoredTerms = new HashSet<ScoredTerm>();

    protected MessagePart() {

    }

    public MessagePart(MimeType mimeType, String content) {
        this.mimeType = mimeType;
        this.content = content;
    }

    public void addScoredTerm(ScoredTerm term) {
        this.scoredTerms.add(term);
    }

    public String getContent() {
        return content;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public Collection<ScoredTerm> getScoredTerms() {
        return scoredTerms;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessagePart [mimeType=");
        builder.append(mimeType);
        builder.append(", scoredTerms=");
        builder.append(scoredTerms);
        builder.append(", content=");
        builder.append(content);
        builder.append(", getGlobalId()=");
        builder.append(getGlobalId());
        builder.append(", getId()=");
        builder.append(getId());
        builder.append("]");
        return builder.toString();
    }

}
