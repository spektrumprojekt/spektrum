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

package de.spektrumprojekt.i.informationextraction;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.commons.chain.CommandChain;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;
import de.spektrumprojekt.informationextraction.extractors.CharNGramsExtractorCommand;
import de.spektrumprojekt.informationextraction.extractors.ExecuteOnlyForExternalMessagesCommand;
import de.spektrumprojekt.informationextraction.extractors.JerichoTextCleanerCommand;
import de.spektrumprojekt.informationextraction.extractors.KeyphraseExtractorCommand;
import de.spektrumprojekt.informationextraction.extractors.LanguageDetectorCommand;
import de.spektrumprojekt.informationextraction.extractors.StemmedTokenExtractorCommand;
import de.spektrumprojekt.informationextraction.extractors.TagExtractorCommand;
import de.spektrumprojekt.informationextraction.extractors.TermCounterCommand;
import de.spektrumprojekt.informationextraction.extractors.WordNGramsExtractorCommand;
import de.spektrumprojekt.persistence.Persistence;

/**
 * Command to extract the information from a message
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class InformationExtractionCommand<T extends MessageFeatureContext> implements Command<T> {

    public static final String PROPERTY_INFORMATION_EXTRACTION_EXECUTION_DATE = "informationExtractionExecutionDate";

    /**
     * TODO use an information extraction configuration
     * 
     * @param informationExtractionConfiguration
     *            TODO
     * 
     * @return a default information extraction command with german english as allowed language
     */
    public static <T extends MessageFeatureContext> InformationExtractionCommand<T> createDefaultGermanEnglish(
            Persistence persistence,
            InformationExtractionConfiguration informationExtractionConfiguration) {
        Collection<String> allowedLanguages = new HashSet<String>();

        allowedLanguages.add("de");
        allowedLanguages.add("en");

        InformationExtractionCommand<T> command = new InformationExtractionCommand<T>(
                persistence, informationExtractionConfiguration);
        if (informationExtractionConfiguration.isDoTokens()
                || informationExtractionConfiguration.isDoKeyphrase()) {
            command.getInformationExtractionCommandChain()
                    .addCommand(
                            new JerichoTextCleanerCommand(
                                    informationExtractionConfiguration.isAddTagsToText()));
        }
        command.getInformationExtractionCommandChain().addCommand(
                new LanguageDetectorCommand("de", allowedLanguages));
        if (informationExtractionConfiguration.isDoTokens()) {

            if (informationExtractionConfiguration.isUseCharNGramsInsteadOfStemming()) {
                command.getInformationExtractionCommandChain().addCommand(
                        new WordNGramsExtractorCommand(
                                informationExtractionConfiguration.isBeMessageGroupSpecific(),
                                false,
                                informationExtractionConfiguration.getnGramsLength(),
                                informationExtractionConfiguration.getMinimumTermLength()));

            } else if (informationExtractionConfiguration.isUseCharNGramsInsteadOfStemming()) {
                command.getInformationExtractionCommandChain().addCommand(
                        new CharNGramsExtractorCommand(
                                informationExtractionConfiguration.isBeMessageGroupSpecific(),
                                false,
                                informationExtractionConfiguration.getnGramsLength(),
                                informationExtractionConfiguration.isCharNGramsRemoveStopwords()));
            } else {
                command.getInformationExtractionCommandChain().addCommand(
                        new StemmedTokenExtractorCommand(
                                informationExtractionConfiguration.isBeMessageGroupSpecific(),
                                false,
                                informationExtractionConfiguration.getMinimumTermLength()));
            }
        }
        if (informationExtractionConfiguration.isDoKeyphrase()) {
            command.getInformationExtractionCommandChain().addCommand(
                    new ExecuteOnlyForExternalMessagesCommand(new KeyphraseExtractorCommand()));
        }
        if (informationExtractionConfiguration.isDoTags()) {
            command.getInformationExtractionCommandChain().addCommand(
                    new TagExtractorCommand(
                            informationExtractionConfiguration.isBeMessageGroupSpecific()));
        }
        if (informationExtractionConfiguration.isMatchTextAgainstTagSource()
                && informationExtractionConfiguration.getTagSource() != null) {
            command.getInformationExtractionCommandChain().addCommand(
                    new ExecuteOnlyForExternalMessagesCommand(new KeyphraseExtractorCommand(
                            informationExtractionConfiguration.getTagSource())));

        }
        if (informationExtractionConfiguration.getTermFrequencyComputer() != null) {
            command.getInformationExtractionCommandChain().addCommand(
                    new TermCounterCommand(persistence,
                            informationExtractionConfiguration.getTermFrequencyComputer()));
        }
        return command;
    }

    public static boolean isInformationExtractionExecuted(Message message) {
        Property property = message.getPropertiesAsMap().get(
                PROPERTY_INFORMATION_EXTRACTION_EXECUTION_DATE);

        boolean hasBeenExecuted = property != null
                && property.getPropertyValue().trim().length() > 0;
        return hasBeenExecuted;
    }

    private final InformationExtractionConfiguration informationExtractionConfiguration;

    private final Persistence persistence;

    private final CommandChain<InformationExtractionContext> informationExtractionCommandChain = new CommandChain<InformationExtractionContext>();

    public InformationExtractionCommand(Persistence persistence,
            InformationExtractionConfiguration informationExtractionConfiguration) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (informationExtractionConfiguration == null) {
            throw new IllegalArgumentException("informationExtractionConfiguration cannot be null.");
        }
        this.persistence = persistence;
        this.informationExtractionConfiguration = informationExtractionConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + ": "
                + this.informationExtractionCommandChain.getConfigurationDescription();
    }

    /**
     * The extraction chain
     * 
     * @return the extraction chain, add commands to it
     */
    public CommandChain<InformationExtractionContext> getInformationExtractionCommandChain() {
        return informationExtractionCommandChain;
    }

    public InformationExtractionConfiguration getInformationExtractionConfiguration() {
        return informationExtractionConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(T context) {

        Message message = context.getMessage();

        boolean hasBeenExecuted = isInformationExtractionExecuted(message);

        // only run if it has not been executed some time for, e.g. if message is presented multiple
        // times to learner or both to learned and ranker at the same time
        if (!hasBeenExecuted) {

            for (MessagePart messagePart : message.getMessageParts()) {
                if (messagePart.isText()) {

                    InformationExtractionContext informationExtractionContext = new InformationExtractionContext(
                            persistence, message, messagePart);

                    informationExtractionCommandChain.process(informationExtractionContext);

                    context.addInformationExtractionContexts(informationExtractionContext);
                }
            }

            message.addProperty(new Property(PROPERTY_INFORMATION_EXTRACTION_EXECUTION_DATE,
                    new Date().getTime() + ""));
        }
    }

}
