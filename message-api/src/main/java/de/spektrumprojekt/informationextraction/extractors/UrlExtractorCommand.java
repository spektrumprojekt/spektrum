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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;

public class UrlExtractorCommand implements Command<InformationExtractionContext> {

    private static final String EXTRACTED_URL = "meta.info.extracted.url";

    private static final Pattern URL_PATTERN = Pattern
            .compile("(?i)\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?Â«Â»â€œâ€�â€˜â€™]))");

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void process(InformationExtractionContext context) {

        Message message = context.getMessage();

        Matcher matcher = URL_PATTERN.matcher(context.getMessagePart().getContent());

        while (matcher.find()) {
            String url = matcher.group();
            message.addProperty(new Property(EXTRACTED_URL, url));

        }

    }

}
