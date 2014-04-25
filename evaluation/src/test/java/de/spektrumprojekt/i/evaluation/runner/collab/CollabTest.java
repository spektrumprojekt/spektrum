package de.spektrumprojekt.i.evaluation.runner.collab;

import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.datamodel.observation.Interest;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.datamodel.observation.ObservationPriority;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.datamodel.test.UserModelTestHelper;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.i.collab.CollaborativeScoreComputer;
import de.spektrumprojekt.i.collab.CollaborativeScoreComputerType;
import de.spektrumprojekt.i.collab.CombiningUserToMessageGroupSpecificTermCollaborativeScoreComputer;
import de.spektrumprojekt.i.collab.UserToMessageCollaborativeScoreComputer;
import de.spektrumprojekt.i.collab.UserToMessageGroupSpecificTermCollaborativeScoreComputer;
import de.spektrumprojekt.i.collab.UserToTermCollaborativeScoreComputer;
import de.spektrumprojekt.i.informationextraction.InformationExtractionCommand;
import de.spektrumprojekt.i.scorer.CollaborativeConfiguration;
import de.spektrumprojekt.i.scorer.ScorerConfiguration;
import de.spektrumprojekt.i.scorer.feature.Feature;
import de.spektrumprojekt.i.term.TermSimilarityWeightComputerFactory;
import de.spektrumprojekt.i.term.TermVectorSimilarityStrategy;
import de.spektrumprojekt.i.term.TermWeightStrategy;
import de.spektrumprojekt.i.term.frequency.TermFrequencyComputer;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

public class CollabTest {

    private SimplePersistence persistence;

    private TermVectorSimilarityComputer termVectorSimilarityComputer;

    private MessageGroup messageGroup;

    private CollaborativeConfiguration create(CollaborativeScoreComputerType type,
            boolean useGeneric) {

        ScorerConfiguration scorerConfiguration = new ScorerConfiguration(
                TermWeightStrategy.INVERSE_TERM_FREQUENCY, TermVectorSimilarityStrategy.COSINUS);
        scorerConfiguration.setFeatureWeight(Feature.COLLABORATION_MATCH_FEATURE, 1.0f, false);

        CollaborativeConfiguration collaborativeConfiguration = new CollaborativeConfiguration(
                scorerConfiguration);
        collaborativeConfiguration.setCollaborativeScoreComputerType(type);
        collaborativeConfiguration.setUseGenericRecommender(useGeneric);

        return collaborativeConfiguration;
    }

    private Message createMessage(String globalId, String content, Term... terms) {
        Message message = new Message(globalId, MessageType.CONTENT, StatusType.OK,
                "sub1", new Date());
        message.addProperty(new Property(
                InformationExtractionCommand.PROPERTY_INFORMATION_EXTRACTION_EXECUTION_DATE,
                new Date().getTime() + ""));

        MessagePart mp = new MessagePart(MimeType.TEXT_PLAIN, content);
        for (Term t : terms) {
            mp.addScoredTerm(new ScoredTerm(t, 1f));
        }
        message.addMessagePart(mp);
        message.setMessageGroup(messageGroup);
        return message;
    }

    @BeforeSuite
    public void initialize() {
        CollaborativeScoreComputer.TEST_MODE = true;

        persistence = new SimplePersistence();

        messageGroup = persistence.storeMessageGroup(new MessageGroup());
        Term[] terms = UserModelTestHelper.getSomeTerms(persistence, messageGroup);
        // create some users
        User user1 = persistence.getOrCreateUser("user1");
        User user2 = persistence.getOrCreateUser("user2");
        User user3 = persistence.getOrCreateUser("user3");

        // create some messages
        persistence.storeMessage(createMessage("messageId1", "some content 1", terms[0], terms[1],
                terms[2]));
        persistence.storeMessage(createMessage("messageId2", "some content 2", terms[1], terms[2]));
        persistence.storeMessage(createMessage("messageId3", "some content 3", terms[0], terms[1]));

        // some user ratings
        persistence.storeObservation(new Observation("user1", "messageId1", ObservationType.RATING,
                ObservationPriority.USER_FEEDBACK, null, new Date(), Interest.EXTREME));
        persistence.storeObservation(new Observation("user1", "messageId2", ObservationType.RATING,
                ObservationPriority.USER_FEEDBACK, null, new Date(), Interest.NONE));
        persistence.storeObservation(new Observation("user1", "messageId3", ObservationType.RATING,
                ObservationPriority.USER_FEEDBACK, null, new Date(), Interest.NORMAL));

        persistence.storeObservation(new Observation("user2", "messageId1", ObservationType.RATING,
                ObservationPriority.USER_FEEDBACK, null, new Date(), Interest.EXTREME));
        persistence.storeObservation(new Observation("user2", "messageId2", ObservationType.RATING,
                ObservationPriority.USER_FEEDBACK, null, new Date(), Interest.NONE));

        // some inferred features
        persistence.storeObservation(new Observation("user1", "messageId1",
                ObservationType.MESSAGE,
                ObservationPriority.FIRST_LEVEL_FEATURE_INFERRED, null, new Date(),
                Interest.EXTREME));
        persistence.storeObservation(new Observation("user2", "messageId2",
                ObservationType.MESSAGE,
                ObservationPriority.FIRST_LEVEL_FEATURE_INFERRED, null, new Date(),
                Interest.EXTREME));
        persistence.storeObservation(new Observation("user1", "messageId3",
                ObservationType.MESSAGE,
                ObservationPriority.FIRST_LEVEL_FEATURE_INFERRED, null, new Date(), Interest.HIGH));
        persistence.storeObservation(new Observation("user2", "messageId3",
                ObservationType.MESSAGE,
                ObservationPriority.FIRST_LEVEL_FEATURE_INFERRED, null, new Date(), Interest.HIGH));

        termVectorSimilarityComputer = TermSimilarityWeightComputerFactory.getInstance()
                .createTermVectorSimilarityComputer(
                        TermVectorSimilarityStrategy.AVG,
                        TermWeightStrategy.TRIVIAL,
                        new TermFrequencyComputer(persistence, true),
                        true);

        UserModel userModel1 = persistence.getOrCreateUserModelByUser(user1.getGlobalId(),
                UserModel.DEFAULT_USER_MODEL_TYPE);
        UserModel userModel2 = persistence.getOrCreateUserModelByUser(user2.getGlobalId(),
                UserModel.DEFAULT_USER_MODEL_TYPE);
        UserModel userModel3 = persistence.getOrCreateUserModelByUser(user3.getGlobalId(),
                UserModel.DEFAULT_USER_MODEL_TYPE);

        UserModelTestHelper.createSomeUserModelEntries(persistence, messageGroup, userModel1,
                userModel2,
                userModel3);
    }

    public void test(
            CollaborativeScoreComputer collaborativeRankerComputer,
            int goalPrefCount,
            int goalMessageScoreWithNaNSize,
            int na,
            boolean test4NonZeroScores
            )
                    throws Exception {
        collaborativeRankerComputer.init();

        if (!(collaborativeRankerComputer instanceof CombiningUserToMessageGroupSpecificTermCollaborativeScoreComputer)) {
            Assert.assertEquals(collaborativeRankerComputer.getPreferenceCount(), goalPrefCount);
        }

        collaborativeRankerComputer.run();

        Assert.assertEquals(collaborativeRankerComputer.getPreferenceCount(), goalPrefCount);

        Assert.assertNotNull(collaborativeRankerComputer.getMessageScores());

        for (UserMessageScore ums : collaborativeRankerComputer.getMessageScores()) {
            if (test4NonZeroScores) {
                Assert.assertTrue(ums.getScore() > 0, ums.toString());
            } else {
                Assert.assertTrue(ums.getScore() >= 0, ums.toString());
            }
        }

        Assert.assertEquals(collaborativeRankerComputer.getMessageScores().size()
                + collaborativeRankerComputer.getNanScores(), goalMessageScoreWithNaNSize);

        Assert.assertTrue(collaborativeRankerComputer.getNanScores() <= na);
    }

    @Test
    public void testCombineU2MGTCollabByMessageObservations() throws Exception {

        test(new CombiningUserToMessageGroupSpecificTermCollaborativeScoreComputer(
                persistence,
                create(CollaborativeScoreComputerType.USER2TERM_PER_MESSAGE_GROUP, true),
                termVectorSimilarityComputer), 9,
                9, 0, true);
        test(new CombiningUserToMessageGroupSpecificTermCollaborativeScoreComputer(persistence,
                create(CollaborativeScoreComputerType.USER2TERM_PER_MESSAGE_GROUP, false),
                termVectorSimilarityComputer),
                9, 9, 0, true);
    }

    @Test
    public void testU2MCollabByMessageObservations() throws Exception {

        test(new UserToMessageCollaborativeScoreComputer(persistence,
                create(CollaborativeScoreComputerType.USER2MESSAGE, true),
                UserToMessageCollaborativeScoreComputer.OT_ONLY_MESSAGE), 4, 6, 2, true);
        test(new UserToMessageCollaborativeScoreComputer(persistence,
                create(CollaborativeScoreComputerType.USER2MESSAGE, false),
                UserToMessageCollaborativeScoreComputer.OT_ONLY_MESSAGE), 4, 6, 2, true);
    }

    @Test
    public void testU2MCollabByRatings() throws Exception {

        test(new UserToMessageCollaborativeScoreComputer(persistence,
                create(CollaborativeScoreComputerType.USER2MESSAGE, true),
                UserToMessageCollaborativeScoreComputer.OT_ONLY_RATINGS), 4, 6, 2, false);
        test(new UserToMessageCollaborativeScoreComputer(persistence,
                create(CollaborativeScoreComputerType.USER2MESSAGE, false),
                UserToMessageCollaborativeScoreComputer.OT_ONLY_RATINGS), 4, 6, 2, false);
    }

    @Test
    public void testU2MGTCollabByMessageObservations() throws Exception {

        test(new UserToMessageGroupSpecificTermCollaborativeScoreComputer(persistence,
                create(CollaborativeScoreComputerType.USER2TERM, true),
                termVectorSimilarityComputer, messageGroup), 9,
                9, 0, true);
        test(new UserToMessageGroupSpecificTermCollaborativeScoreComputer(
                persistence,
                create(CollaborativeScoreComputerType.USER2TERM, false),
                termVectorSimilarityComputer,
                messageGroup),
                9, 9, 0, true);
    }

    @Test
    public void testU2TCollabByMessageObservations() throws Exception {

        test(new UserToTermCollaborativeScoreComputer(persistence,
                create(CollaborativeScoreComputerType.USER2TERM, true),
                termVectorSimilarityComputer), 9,
                9, 0, true);
        test(new UserToTermCollaborativeScoreComputer(persistence,
                create(CollaborativeScoreComputerType.USER2TERM, false),
                termVectorSimilarityComputer),
                9, 9, 0, true);
    }

    @Test
    public void testU2TCollabByRatings() throws Exception {

        test(new UserToTermCollaborativeScoreComputer(persistence,
                create(CollaborativeScoreComputerType.USER2TERM, true),
                termVectorSimilarityComputer),
                9, 9, 0, true);
        test(new UserToTermCollaborativeScoreComputer(
                persistence,
                create(CollaborativeScoreComputerType.USER2TERM, false),
                termVectorSimilarityComputer
                ),
                9, 9, 0, true);
    }
}
