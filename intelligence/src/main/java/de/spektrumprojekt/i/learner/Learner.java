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

package de.spektrumprojekt.i.learner;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.chain.CommandChain;
import de.spektrumprojekt.commons.chain.ProxyCommand;
import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.MessageHandler;
import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.i.informationextraction.InformationExtractionCommand;
import de.spektrumprojekt.i.learner.chain.LoadRelatedObservationsCommand;
import de.spektrumprojekt.i.learner.chain.StoreObservationCommand;
import de.spektrumprojekt.i.learner.chain.UserModelLearnerCommand;
import de.spektrumprojekt.i.learner.time.TimeBinnedUserModelEntryIntegrationStrategy;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.ranker.Ranker;
import de.spektrumprojekt.i.ranker.RankerConfiguration;
import de.spektrumprojekt.i.ranker.UserModelConfiguration;
import de.spektrumprojekt.i.timebased.TermCounterCommand;
import de.spektrumprojekt.persistence.Persistence;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class Learner implements MessageHandler<LearningMessage>, ConfigurationDescriptable {

    private final CommandChain<LearnerMessageContext> learnerChain;

    private final Persistence persistence;

    private final static Logger LOGGER = LoggerFactory.getLogger(Ranker.class);

    /**
     * TODO use a LearnerConfiguration
     * 
     * @param persistence
     *            the persistence
     * @param userModelEntryIntegrationStrategy
     *            the strategy to integrate the user model
     */
    public Learner(Persistence persistence, RankerConfiguration configuration,
            InformationExtractionCommand<MessageFeatureContext> ieChain) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null!");
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration cannot be null!");
        }
        // if (userModelEntryIntegrationStrategy == null) {
        // throw new IllegalArgumentException("userModelEntryIntegrationStrategy cannot be null!");
        // }
        this.persistence = persistence;

        this.learnerChain = new CommandChain<LearnerMessageContext>();

        this.learnerChain
                .addCommand(new ProxyCommand<MessageFeatureContext, LearnerMessageContext>(ieChain));
        this.learnerChain.addCommand(new LoadRelatedObservationsCommand(this.persistence));
        for (String userModelType : configuration.getUserModelTypes().keySet()) {
            UserModelEntryIntegrationStrategy userModelEntryIntegrationStrategy;
            UserModelConfiguration userModelConfiguration = configuration.getUserModelTypes().get(
                    userModelType);
            switch (userModelConfiguration.getUserModelEntryIntegrationStrategy()) {
            case PLAIN:
                userModelEntryIntegrationStrategy = new UserModelEntryIntegrationPlainStrategy();
                break;
            default:
                userModelEntryIntegrationStrategy = new TimeBinnedUserModelEntryIntegrationStrategy(
                        userModelConfiguration.getStartTime(), userModelConfiguration.getBinSize(),
                        userModelConfiguration.getPrecision(),
                        userModelConfiguration.isCalculateLater());
            }
            this.learnerChain.addCommand(new UserModelLearnerCommand(this.persistence,
                    userModelType, userModelEntryIntegrationStrategy, configuration
                            .isCreateUnknownTermsInUsermodel(userModelType)));
        }
        this.learnerChain.addCommand(new StoreObservationCommand(this.persistence));
        this.learnerChain.addCommand(new TermCounterCommand(configuration, this.persistence));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deliverMessage(LearningMessage learningMessage) {

        StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        Message message = this.persistence.getMessageByGlobalId(learningMessage.getObservation()
                .getMessageGlobalId());
        if (message == null) {
            throw new IllegalStateException("Message for globalId="
                    + learningMessage.getObservation().getMessageGlobalId()
                    + " not found in persistence.");
        }
        LearnerMessageContext context = new LearnerMessageContext(persistence,
                learningMessage.getObservation(), message, learningMessage.getMessageRelation());

        this.learnerChain.process(context);

        stopWatch.stop();

        LOGGER.trace("Learner processed message {} in {} ms", context.getMessage().getGlobalId(),
                stopWatch.getTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " learnerChain: "
                + this.learnerChain.getConfigurationDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<LearningMessage> getMessageClass() {
        return LearningMessage.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(CommunicationMessage message) {
        return message instanceof LearningMessage;
    }

}
