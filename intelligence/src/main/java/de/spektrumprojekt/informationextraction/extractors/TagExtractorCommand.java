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

package de.spektrumprojekt.informationextraction.extractors;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;

public final class TagExtractorCommand implements Command<InformationExtractionContext> {

    private final boolean useMessageGroupIdForToken;
    private final boolean assertMessageGroup;

    public TagExtractorCommand(boolean useMessageGroupIdForToken) {
        this(useMessageGroupIdForToken, true);
    }

    public TagExtractorCommand(boolean useMessageGroupIdForToken,
            boolean assertMessageGroup) {
        this.useMessageGroupIdForToken = useMessageGroupIdForToken;
        this.assertMessageGroup = assertMessageGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " useMessageGroupIdForToken: "
                + useMessageGroupIdForToken + " assertMessageGroup: " + assertMessageGroup;
    }

    public boolean isAssertMessageGroup() {
        return assertMessageGroup;
    }

    public boolean isUseMessageGroupIdForToken() {
        return useMessageGroupIdForToken;
    }

    @Override
    public void process(InformationExtractionContext context) {

        Collection<String> tags = MessageHelper.getTags(context.getMessage());

        String tokenPrefix = StringUtils.EMPTY;
        if (this.useMessageGroupIdForToken) {
            MessageGroup group = context.getMessage().getMessageGroup();
            if (group == null) {
                if (this.assertMessageGroup) {
                    throw new IllegalStateException("messagegroup not set for message="
                            + context.getMessage());
                }
            } else {
                tokenPrefix = group.getId() + "#";
            }
        }
        for (String tag : tags) {
            String term = tokenPrefix + "%" + tag;
            context.getMessagePart().addScoredTerm(
                    new ScoredTerm(context.getPersistence().getOrCreateTerm(
                            Term.TermCategory.TERM,
                            term),
                            1));
        }

    }

}
