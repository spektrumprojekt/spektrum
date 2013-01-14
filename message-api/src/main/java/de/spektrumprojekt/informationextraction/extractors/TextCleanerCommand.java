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

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;

public final class TextCleanerCommand implements Command<InformationExtractionContext> {

    public static final String CLEAN_TEXT = "messagepart.text.clean";

    private static final Pattern STRIP_HTML = Pattern
            .compile("<!--.*?-->|<style.*?>.*?</style>|<script.*?>.*?</script>|<.*?>");

    private static final Pattern NORMALIZE_WHITESPACE = Pattern.compile("\\s{2,}");

    private String cleanText(String rawText) {
        String cleanText = STRIP_HTML.matcher(rawText).replaceAll("");
        cleanText = NORMALIZE_WHITESPACE.matcher(cleanText).replaceAll(" ");
        cleanText = cleanText.trim();
        return cleanText;

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
