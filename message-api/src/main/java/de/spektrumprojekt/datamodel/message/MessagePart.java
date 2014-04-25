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

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.identifiable.Identifiable;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public class MessagePart extends Identifiable {

    private static final long serialVersionUID = 1L;

    private String mimeType;

    @Lob
    private String content;

    private int contentLength = -1;

    @OneToMany(cascade = CascadeType.ALL)
    private final Collection<ScoredTerm> scoredTerms = new HashSet<ScoredTerm>();

    protected MessagePart() {

    }

    public MessagePart(MimeType mimeType, String content) {
        this(mimeType.getTypeIdentifier(), content);
    }

    public MessagePart(String mimeType, String content) {
        super();
        this.mimeType = mimeType;
        this.content = content;
    }

    public MessagePart(String globalId, String mimeType, String content) {
        super(globalId);
        this.mimeType = mimeType;
        this.content = content;
    }

    public void addScoredTerm(ScoredTerm term) {
        this.scoredTerms.add(term);
    }

    public String getContent() {
        return content;
    }

    public int getContentLength() {
        return contentLength < 0 && content != null ? content.length() : contentLength;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Collection<ScoredTerm> getScoredTerms() {
        return scoredTerms;
    }

    @JsonIgnore
    public boolean isAttachment() {
        return !(MimeType.JSON.getTypeIdentifier().equals(mimeType)
                || MimeType.XML.getTypeIdentifier().equals(mimeType)
                || MimeType.TEXT_HTML.getTypeIdentifier().equals(mimeType)
                || MimeType.TEXT_PLAIN.getTypeIdentifier()
                .equals(mimeType));
    }

    @JsonIgnore
    public boolean isImageAttachment() {
        return this.mimeType != null && this.mimeType.startsWith("image");
    }

    @JsonIgnore
    public boolean isMimeType(MimeType mimeType) {
        return mimeType == null && this.mimeType == null
                || StringUtils.equals(mimeType.getTypeIdentifier(), this.mimeType);
    }

    @JsonIgnore
    public boolean isText() {
        return isMimeType(MimeType.TEXT_HTML) || isMimeType(MimeType.TEXT_PLAIN);
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public String toString() {
        return "MessagePart [mimeType=" + mimeType + ", content=" + content + ", contentLength="
                + contentLength + ", scoredTerms=" + scoredTerms + "]";
    }

}
