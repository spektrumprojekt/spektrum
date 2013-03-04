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
import org.apache.commons.lang.StringUtils;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;

public class KeyphraseExtractorCommand implements Command<InformationExtractionContext> {

    /** Allowed characters for a token, everything else will be filtered out. */
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z0-9\\.]+");

    /** Marker to indicate an undesired token. */
    private static final String IGNORED_TOKEN = "###";

    private KeyphraseCandidates createCandidates(String language, Bag nGramBag) {
        KeyphraseCandidates candidates = new KeyphraseCandidates();
        for (Object nGramObj : nGramBag) {
            String nGram = (String) nGramObj;
            String stemmedNGram = ExtractionUtils.stem(language, nGram);
            candidates.addCandidate(stemmedNGram, nGram, nGramBag.getCount(nGram));
        }
        candidates.removeOverlaps();
        return candidates;
    }

    /**
     * <p>
     * Filter tokens which are stop words, or which are not matched by the {@link #TOKEN_PATTERN},
     * or with a length of one and no character.
     * </p>
     * 
     * @param tokens
     * @return
     */
    private List<String> filterTokens(String language, List<String> tokens) {
        List<String> filteredTokens = new ArrayList<String>();
        for (String token : tokens) {
            if (ExtractionUtils.isStopword(language, token)) {
                filteredTokens.add(IGNORED_TOKEN);
            } else if (!TOKEN_PATTERN.matcher(token).matches()) {
                filteredTokens.add(IGNORED_TOKEN);
            } else if (token.length() == 1 && !Character.isLetter(token.charAt(0))) {
                filteredTokens.add(IGNORED_TOKEN);
            } else {
                filteredTokens.add(token);
            }
        }
        return filteredTokens;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void process(InformationExtractionContext context) {

        String language = LanguageDetectorCommand.getAnnotatedLanguage(context.getMessage());

        String text = context.getCleanText();
        if (StringUtils.isEmpty(text)) {
            return;
        }

        text = text.toLowerCase();
        List<String> tokens = ExtractionUtils.tokenize(text);
        tokens = filterTokens(language, tokens);

        List<String> nGrams = new ArrayList<String>();
        nGrams.addAll(tokens);
        nGrams.addAll(ExtractionUtils.createNGrams(tokens, 2));
        nGrams.addAll(ExtractionUtils.createNGrams(tokens, 3));
        nGrams.addAll(ExtractionUtils.createNGrams(tokens, 4));

        Bag nGramBag = new HashBag();
        for (String nGram : nGrams) {
            if (nGram.contains(IGNORED_TOKEN) || nGram.length() < 3) {
                continue;
            }
            nGramBag.add(nGram);
        }

        KeyphraseCandidates candidates = createCandidates(language, nGramBag);

        for (KeyphraseCandidate candidate : candidates) {
            if (candidate.getCount() > 1) {

                // TODO how to normalize the score ?
                context.getMessagePart().addScoredTerm(
                        new ScoredTerm(
                                context.getPersistence().getOrCreateTerm(
                                        Term.TermCategory.KEYPHRASE,
                                        candidate.getShortestUnstemmedValue()),
                                candidate.getCount()));
            }
        }
    }

}
