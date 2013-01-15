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
import java.util.HashSet;

import me.champeau.ld.UberLanguageDetector;
import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;

public final class LanguageDetectorCommand implements Command<InformationExtractionContext> {

    public static final String LANGUAGE = "meta.info.language";

    /**
     * <p>
     * Convenience method to get the annotated language after the {@link Message} has been
     * {@link #process(Message)}ed by the {@link LanguageDetectorCommand}.
     * </p>
     * 
     * @param message
     * @return
     */
    public static String getAnnotatedLanguage(Message message) {
        Property property = message.getPropertiesAsMap().get(LANGUAGE);
        return property == null ? null : property.getPropertyValue();
    }

    private String defaultLanguage;

    private Collection<String> allowedLanguages;

    private final UberLanguageDetector languageDetector;

    /**
     * 
     * @param defaultLanguage
     *            the default language is used if no language is returned for the text. can not be
     *            null.
     */
    public LanguageDetectorCommand(String defaultLanguage) {
        this(defaultLanguage, new HashSet<String>());
    }

    /**
     * 
     * @param defaultLanguage
     *            the default language is used if no language is returned for the text. can not be
     *            null.
     * @param allowedLanguages
     *            if not null and not empty the determined language will be lower cased and checked
     *            against the languages codes
     */
    public LanguageDetectorCommand(String defaultLanguage, Collection<String> allowedLanguages) {
        if (defaultLanguage == null) {
            throw new IllegalArgumentException(
                    "DefaultLanguage must be set if the allowedLanguages are used!");
        }
        this.defaultLanguage = defaultLanguage;
        this.allowedLanguages = new HashSet<String>(allowedLanguages);
        try {
            Class<?> clazz = this.getClass().getClassLoader()
                    .loadClass("me.champeau.ld.UberLanguageDetector");
            clazz.getClassLoader();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        languageDetector = UberLanguageDetector.getInstance();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + ": defaultLanguage=" + this.defaultLanguage
                + " allowedLanguages=" + this.allowedLanguages;
    }

    @Override
    public void process(InformationExtractionContext context) {
        Message message = context.getMessage();
        String cleanedText = context.getCleanText();
        if (cleanedText == null) {
            return;
        }
        String language = languageDetector.detectLang(cleanedText);

        if (language == null) {
            // if language is null use the default langugage.
            language = this.defaultLanguage;
        }
        else if (this.allowedLanguages != null && this.allowedLanguages.size() > 0) {
            // if allowed languages are set
            // check if the language is not contained
            if (!this.allowedLanguages.contains(language.toLowerCase())) {
                // and use default
                language = this.defaultLanguage;
            }
        }
        message.addProperty(new Property(LANGUAGE, language));
    }

}
