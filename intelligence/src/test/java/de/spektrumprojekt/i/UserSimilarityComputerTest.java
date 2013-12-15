package de.spektrumprojekt.i;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.test.UserModelTestHelper;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.learner.adaptation.UserModelBasedSimilarityConfiguration;
import de.spektrumprojekt.i.similarity.set.JaccardSetSimilarity;
import de.spektrumprojekt.i.similarity.user.InteractionBasedUserSimilarityComputer;
import de.spektrumprojekt.i.similarity.user.UserModelBasedUserSimilarityComputer;
import de.spektrumprojekt.i.similarity.user.UserSimilaritySimType;
import de.spektrumprojekt.i.term.TermSimilarityWeightComputerFactory;
import de.spektrumprojekt.i.term.TermVectorSimilarityStrategy;
import de.spektrumprojekt.i.term.TermWeightStrategy;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;

/**
 * Test the ranker
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UserSimilarityComputerTest extends IntelligenceSpektrumTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserSimilarityComputerTest.class);

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
    public void testInteractionBasedUserSimilarityComputer() throws Exception {
        InteractionBasedUserSimilarityComputer userSimilarityComputer = new InteractionBasedUserSimilarityComputer(
                getPersistence(), UserSimilaritySimType.FROM_PERCENTAGE);

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

    /**
     * Test
     * 
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testUserModelBasedUserSimilarityComputer() throws Exception {

        TermVectorSimilarityComputer termVectorSimilarityComputer;

        termVectorSimilarityComputer = TermSimilarityWeightComputerFactory.getInstance()
                .createTermVectorSimilarityComputer(
                        TermVectorSimilarityStrategy.COSINUS,
                        TermWeightStrategy.TRIVIAL,
                        null,
                        false);

        UserModelBasedSimilarityConfiguration conf = new UserModelBasedSimilarityConfiguration();
        conf.setSetSimilarity(new JaccardSetSimilarity());
        UserModelBasedUserSimilarityComputer userSimilarityComputer = new UserModelBasedUserSimilarityComputer(
                getPersistence(),
                conf,
                termVectorSimilarityComputer);

        // create some users

        User user1 = getPersistence().getOrCreateUser("user_1_" + UUID.randomUUID());
        User user2 = getPersistence().getOrCreateUser("user_2_" + UUID.randomUUID());
        User user3 = getPersistence().getOrCreateUser("user_3_" + UUID.randomUUID());

        UserModel userModel1 = getPersistence().getOrCreateUserModelByUser(user1.getGlobalId(),
                UserModel.DEFAULT_USER_MODEL_TYPE);
        UserModel userModel2 = getPersistence().getOrCreateUserModelByUser(user2.getGlobalId(),
                UserModel.DEFAULT_USER_MODEL_TYPE);
        UserModel userModel3 = getPersistence().getOrCreateUserModelByUser(user3.getGlobalId(),
                UserModel.DEFAULT_USER_MODEL_TYPE);

        MessageGroup mg = new MessageGroup();
        mg = getPersistence().storeMessageGroup(mg);

        UserModelTestHelper.createSomeUserModelEntries(getPersistence(), mg, userModel1,
                userModel2, userModel3);

        // run computation
        userSimilarityComputer.run();

        // check results
        Collection<UserSimilarity> similarities = getPersistence().getUserSimilarities(
                user1.getGlobalId(),
                Arrays.asList(user2.getGlobalId(), user3.getGlobalId()),
                mg.getGlobalId(),
                0);
        Assert.assertNotNull(similarities);
        Assert.assertEquals(2, similarities.size());

        for (UserSimilarity us : similarities) {
            Assert.assertTrue("" + us, us.getSimilarity() > 0.25f);

            System.out.println("us: " + us);
        }
    }
}
