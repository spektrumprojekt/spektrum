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

package de.spektrumprojekt.i;

import java.io.File;
import java.util.Date;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Assert;

import de.spektrumprojekt.configuration.properties.XmlPropertiesConfiguration;
import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.jpa.JPAPersistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

/**
 * General test helper class
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public abstract class IntelligenceSpektrumTest {

    private Persistence persistence;

    private String configurationRootDir = "src/test/resources/cfg";
    private XmlPropertiesConfiguration configuration;

    /**
     * Checks that the contexts first message part has scored terms. Fails if there is no message
     * part and if the this first message part does not have at least one scored term
     * 
     * @param context
     *            the context to check
     */
    protected void checkScoredTerms(MessageFeatureContext context) {
        MessagePart messagePart = context.getMessage().getMessageParts().iterator().next();
        Assert.assertNotNull(messagePart.getScoredTerms());
        Assert.assertTrue("Must have some scored terms. size="
                + messagePart.getScoredTerms().size(), messagePart.getScoredTerms().size() > 0);

    }

    /**
     * Create a plain text message
     * 
     * @param content
     *            the content
     * @param authorGlobalId
     *            the author
     * @param messageGroup
     *            the message group
     * @return the message
     */
    protected Message createPlainTextMessage(String content, String authorGlobalId,
            MessageGroup messageGroup) {
        Message message = new Message(MessageType.CONTENT, StatusType.OK, "subscriptionId",
                new Date());
        MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN,
                content);
        message.addMessagePart(messagePart);

        // TODO once only id another time message group, use messageGroupGlobalId instead ?
        message.setAuthorGlobalId(authorGlobalId);
        message.setMessageGroup(messageGroup);
        return message;
    }

    /**
     * 
     * @return the configuration root directory
     */
    public String getConfigurationRootDir() {
        return configurationRootDir;
    }

    /**
     * 
     * @return the persistence
     */
    public Persistence getPersistence() {
        if (persistence == null) {
            throw new IllegalArgumentException(
                    "persistence is null. Call #setupPersistence before.");
        }
        return persistence;
    }

    /**
     * Setups the persistence, call this in a BeforeTest method.
     * 
     * @throws ConfigurationException
     *             in error
     */
    protected void setupPersistence() throws ConfigurationException {
        // messageGroup = getMessageGroup(note.getBlog());
        // authorGlobalId = GlobalIdHelper.buildGlobalIdIString(GlobalIdType.USER, note.getId());

        File file = new File(this.configurationRootDir
                + File.separator + "mystream.test.properties.xml");
        String filename = file.getAbsolutePath();
        this.configuration = new XmlPropertiesConfiguration(filename);
        this.persistence = new JPAPersistence(this.configuration);

        this.persistence = new SimplePersistence();
        this.persistence.initialize();
    }

}
