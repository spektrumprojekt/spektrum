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

import java.util.regex.Pattern;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;

/***
 * TODO maybe think about using something like this: http://jericho.htmlparser.net/docs/index.html
 * it may provide more support about html encoding etc.
 * 
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 * 
 */
public final class JerichoTextCleanerCommand implements Command<InformationExtractionContext> {

    private static final int CHARACTER_WHITESPACE = 32;
    private static final char CHARACHTER_HASH = '#';
    private static final Pattern NORMALIZE_WHITESPACE = Pattern.compile("\\s{2,}");

    public JerichoTextCleanerCommand() {
    }

    private String cleanText(String rawText) {

        Source source = new Source(rawText);

        TextExtractor extractor = source.getTextExtractor().setConvertNonBreakingSpaces(true)
                .setIncludeAttributes(false);
        String cleanText = extractor.toString();
        char[] cha = cleanText.toCharArray();
        for (int i = 0; i < cha.length; i++) {
            if (Character.isWhitespace(cha[i])) {
                cha[i] = CHARACTER_WHITESPACE;
            } else if (cha[i] == CHARACHTER_HASH) {
                cha[i] = CHARACTER_WHITESPACE;
            }
        }
        cleanText = new String(cha);
        cleanText = NORMALIZE_WHITESPACE.matcher(cleanText).replaceAll(" ");
        return cleanText.trim();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(InformationExtractionContext context) {

        MessagePart rawTextPart = context.getMessagePart();

        String rawText = rawTextPart.getContent();
        String cleanText = cleanText(rawText);

        context.setCleanText(cleanText);
    }
}
