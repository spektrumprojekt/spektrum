package de.spektrumprojekt.i;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.user.similarity.UserSimilarityComputer;

/**
 * Test the ranker
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UserSimilarityComputerTest extends IntelligenceSpektrumTest {

    @Before
    public void setupTest() throws ConfigurationException {
        setupPersistence();
    }

    /**
     * Test
     * 
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testUserSimilarityComputer() throws Exception {
        UserSimilarityComputer userSimilarityComputer = new UserSimilarityComputer(getPersistence());

        // create some users

        User user1 = getPersistence().getOrCreateUser("user_1_" + UUID.randomUUID());
        User user2 = getPersistence().getOrCreateUser("user_2_" + UUID.randomUUID());
        User user3 = getPersistence().getOrCreateUser("user_3_" + UUID.randomUUID());

        // TODO create some messages for users

        MessageGroup messageGroup = getPersistence().storeMessageGroup(new MessageGroup());
        Message message = createPlainTextMessage(
                "i am a weird interesting text message " + UUID.randomUUID(), user1.getGlobalId(),
                messageGroup);

        message.addProperty(MessageHelper
                .createMentionProperty(Arrays.asList(user2.getGlobalId(), user3.getGlobalId())));
        getPersistence().storeMessage(message);

        // run computation
        userSimilarityComputer.run();

        // check results
        Collection<UserSimilarity> similarities = getPersistence().getUserSimilarities(
                user1.getGlobalId(), Arrays.asList(user2.getGlobalId(), user3.getGlobalId()),
                messageGroup.getGlobalId(),
                0);
        Assert.assertNotNull(similarities);
        Assert.assertEquals(2, similarities.size());
    }
}
