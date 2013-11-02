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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;
import org.apache.commons.lang3.StringUtils;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;

public final class StemmedTokenExtractorCommand implements Command<InformationExtractionContext> {

    /** Allowed characters for a token, everything else will be filtered out. */
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z0-9\\.]+");

    private final boolean useMessageGroupIdForToken;
    private final boolean assertMessageGroup;
    private final int minimumTermLength;

    public StemmedTokenExtractorCommand(boolean useMessageGroupIdForToken) {
        this(useMessageGroupIdForToken, true, 0);
    }

    public StemmedTokenExtractorCommand(boolean useMessageGroupIdForToken,
            boolean assertMessageGroup, int minimumTermLength) {
        this.useMessageGroupIdForToken = useMessageGroupIdForToken;
        this.assertMessageGroup = assertMessageGroup;
        this.minimumTermLength = minimumTermLength;
    }

    private List<String> cleanTokens(List<String> tokens) {
        List<String> cleanTokens = new ArrayList<String>();
        for (String token : tokens) {
            if (token.length() < 2) {
                continue;
            }
            if (!TOKEN_PATTERN.matcher(token).matches()) {
                continue;
            }

            cleanTokens.add(token.toLowerCase());
        }
        return cleanTokens;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + " useMessageGroupIdForToken: " + useMessageGroupIdForToken
                + " assertMessageGroup: " + assertMessageGroup
                + " minimumTermLength: " + minimumTermLength;
    }

    public boolean isAssertMessageGroup() {
        return assertMessageGroup;
    }

    public boolean isUseMessageGroupIdForToken() {
        return useMessageGroupIdForToken;
    }

    @Override
    public void process(InformationExtractionContext context) {

        String language = LanguageDetectorCommand.getAnnotatedLanguage(context.getMessage());

        String text = context.getCleanText();
        if (StringUtils.isEmpty(text)) {
            return;
        }

        List<String> tokens = ExtractionUtils.tokenize(text);
        tokens = removeStopwords(language, tokens);
        tokens = ExtractionUtils.stem(language, tokens);
        tokens = cleanTokens(tokens);

        Bag tokenBag = new HashBag(tokens);
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
        int highestCount = BagHelper.getHighestCount(tokenBag);
        for (Object tokenObj : tokenBag) {
            String token = (String) tokenObj;
            if (token.length() < minimumTermLength) {
                continue;
            }
            float frequency = (float) tokenBag.getCount(token) / highestCount;
            token = tokenPrefix + token;
            context.getMessagePart().addScoredTerm(
                    new ScoredTerm(context.getPersistence().getOrCreateTerm(
                            Term.TermCategory.TERM,
                            token),
                            frequency));
        }

    }

    private List<String> removeStopwords(String language, List<String> tokens) {
        List<String> ret = new ArrayList<String>();
        for (String token : tokens) {
            if (!ExtractionUtils.isStopword(language, token.toLowerCase())) {
                ret.add(token);
            }
        }
        return ret;
    }

}
