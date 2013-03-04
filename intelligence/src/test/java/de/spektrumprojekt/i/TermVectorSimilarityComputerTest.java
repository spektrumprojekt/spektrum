package de.spektrumprojekt.i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.term.TermSimilarityWeightComputerFactory;
import de.spektrumprojekt.i.term.TermVectorSimilarityStrategy;
import de.spektrumprojekt.i.term.TermWeightStrategy;
import de.spektrumprojekt.i.term.frequency.TermFrequencyComputer;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.i.term.weight.TermWeightComputer;

public class TermVectorSimilarityComputerTest extends IntelligenceSpektrumTest {

    private TermFrequencyComputer termFrequencyComputer;

    private List<Message> messages = new ArrayList<Message>();

    private String[] terms = new String[] { "A", "B", "C", "D", "E", "F", "G" };

    private Map<String, Float> trivalWeights = new HashMap<String, Float>();

    private Map<String, Float> expectedLogInverseWeights = new HashMap<String, Float>();

    private Map<String, Float> expectedLinearInverseWeights = new HashMap<String, Float>();

    private Map<String, Integer> exptectedTermCounts = new HashMap<String, Integer>();

    private final static float EPSILON = 0.000001f;

    private Map<Long, Float> expectedSimilarityTrivialCos = new HashMap<Long, Float>();
    private Map<Long, Float> expectedSimilarityTrivialAvg = new HashMap<Long, Float>();
    private Map<Long, Float> expectedSimilarityTrivialMax = new HashMap<Long, Float>();

    private Map<Long, Float> expectedSimilarityLogInvCos = new HashMap<Long, Float>();
    private Map<Long, Float> expectedSimilarityLogInvAvg = new HashMap<Long, Float>();
    private Map<Long, Float> expectedSimilarityLogInvMax = new HashMap<Long, Float>();

    private Map<Long, Float> expectedSimilarityLinInvCos = new HashMap<Long, Float>();
    private Map<Long, Float> expectedSimilarityLinInvAvg = new HashMap<Long, Float>();
    private Map<Long, Float> expectedSimilarityLinInvMax = new HashMap<Long, Float>();

    private Map<Term, UserModelEntry> userModelEntriesMap = new HashMap<Term, UserModelEntry>();

    private void assertTermCounts() {
        for (Entry<String, Integer> entry : this.exptectedTermCounts.entrySet()) {
            Term term = getPersistence().getOrCreateTerm(TermCategory.TERM, entry.getKey());

            Assert.assertNotNull(entry.getKey(), term);
            Assert.assertEquals(entry.getKey(), entry.getValue().intValue(), term.getCount());
        }
    }

    private void computeMessageSimilarity(TermVectorSimilarityStrategy simStrategy,
            TermWeightStrategy weightStrategy, Map<Long, Float> expectedSimilarity) {
        TermVectorSimilarityComputer termVectorSimilarityComputer;

        termVectorSimilarityComputer = TermSimilarityWeightComputerFactory.getInstance()
                .createTermVectorSimilarityComputer(
                        simStrategy,
                        weightStrategy,
                        termFrequencyComputer,
                        false);

        for (Message message : this.messages) {

            Collection<Term> terms = MessageHelper.getAllTerms(message);
            Map<Term, UserModelEntry> relevant = new HashMap<Term, UserModelEntry>();
            for (Term t : terms) {
                UserModelEntry ume = this.userModelEntriesMap.get(t);
                if (ume != null) {
                    relevant.put(t, ume);
                }
            }

            float sim = termVectorSimilarityComputer.getSimilarity(null, relevant,
                    terms);
            float expectedSim = expectedSimilarity.get(message.getId());

            Assert.assertEquals(simStrategy + " " + weightStrategy + " " + message.getId(),
                    expectedSim, sim, EPSILON);
        }
    }

    private Message createMessage(Long msgId, String whitespacedTterms) {
        String[] terms = StringUtils.split(whitespacedTterms, " ");
        Message message = new Message("" + msgId, MessageType.CONTENT, StatusType.OK, null,
                new Date());
        message.setId(msgId);
        MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN, UUID.randomUUID().toString());

        for (String t : terms) {
            Term term = getPersistence().getOrCreateTerm(TermCategory.TERM, t);
            messagePart.getScoredTerms().add(new ScoredTerm(term, 1));
        }

        message.addMessagePart(messagePart);
        return message;
    }

    private void createMessages() {

        messages.add(createMessage(1l, "A B C D E F"));
        messages.add(createMessage(2l, "A B C D"));
        messages.add(createMessage(3l, "A B"));
        messages.add(createMessage(4l, "A"));
        messages.add(createMessage(5l, "C D F"));
        messages.add(createMessage(6l, "D F"));
        messages.add(createMessage(7l, "B D"));
        messages.add(createMessage(8l, "E F G"));
        messages.add(createMessage(9l, "A E"));
        messages.add(createMessage(10l, "A B E"));

    }

    @Before
    public void init() throws ConfigurationException {

        super.setupPersistence();

        this.initExpectedValues();

        this.createMessages();

        this.initUserModel();

        this.initTermFrequencies();

    }

    private void initExpectedValues() {

        expectedSimilarityLogInvCos.put(1l, 0.922779804f);
        expectedSimilarityLogInvCos.put(2l, 0.925093515f);
        expectedSimilarityLogInvCos.put(3l, 0.985336854f);
        expectedSimilarityLogInvCos.put(4l, 1f);
        expectedSimilarityLogInvCos.put(5l, 0.914167774f);
        expectedSimilarityLogInvCos.put(6l, 0.998222839f);
        expectedSimilarityLogInvCos.put(7l, 0.948683298f);
        expectedSimilarityLogInvCos.put(8l, 0.945363691f);
        expectedSimilarityLogInvCos.put(9l, 0.951972267f);
        expectedSimilarityLogInvCos.put(10l, 0.940708269f);

        expectedSimilarityLogInvMax.put(1l, 0.549774439f);
        expectedSimilarityLogInvMax.put(2l, 0.361191841f);
        expectedSimilarityLogInvMax.put(3l, 0.138629436f);
        expectedSimilarityLogInvMax.put(4l, 0.051082562f);
        expectedSimilarityLogInvMax.put(5l, 0.549774439f);
        expectedSimilarityLogInvMax.put(6l, 0.549774439f);
        expectedSimilarityLogInvMax.put(7l, 0.277258872f);
        expectedSimilarityLogInvMax.put(8l, 1.611809565f);
        expectedSimilarityLogInvMax.put(9l, 0.458145366f);
        expectedSimilarityLogInvMax.put(10l, 0.458145366f);

        expectedSimilarityLogInvAvg.put(1l, 0.372153171f);
        expectedSimilarityLogInvAvg.put(2l, 0.267055122f);
        expectedSimilarityLogInvAvg.put(3l, 0.157571664f);
        expectedSimilarityLogInvAvg.put(4l, 0.1f);
        expectedSimilarityLogInvAvg.put(5l, 0.422343295f);
        expectedSimilarityLogInvAvg.put(6l, 0.513864688f);
        expectedSimilarityLogInvAvg.put(7l, 0.3f);
        expectedSimilarityLogInvAvg.put(8l, 0.633524511f);
        expectedSimilarityLogInvAvg.put(9l, 0.356822992f);
        expectedSimilarityLogInvAvg.put(10l, 0.305555113f);

        expectedSimilarityLinInvCos.put(1l, 0.92756397f);
        expectedSimilarityLinInvCos.put(2l, 0.936382184f);
        expectedSimilarityLinInvCos.put(3l, 0.977802414f);
        expectedSimilarityLinInvCos.put(4l, 1f);
        expectedSimilarityLinInvCos.put(5l, 0.940003488f);
        expectedSimilarityLinInvCos.put(6l, 0.994309154f);
        expectedSimilarityLinInvCos.put(7l, 0.948683298f);
        expectedSimilarityLinInvCos.put(8l, 0.994369174f);
        expectedSimilarityLinInvCos.put(9l, 0.924678098f);
        expectedSimilarityLinInvCos.put(10l, 0.915475416f);

        expectedSimilarityLinInvMax.put(1l, 0.36f);
        expectedSimilarityLinInvMax.put(2l, 0.21f);
        expectedSimilarityLinInvMax.put(3l, 0.1f);
        expectedSimilarityLinInvMax.put(4l, 0.04f);
        expectedSimilarityLinInvMax.put(5l, 0.36f);
        expectedSimilarityLinInvMax.put(6l, 0.36f);
        expectedSimilarityLinInvMax.put(7l, 0.2f);
        expectedSimilarityLinInvMax.put(8l, 0.63f);
        expectedSimilarityLinInvMax.put(9l, 0.3f);
        expectedSimilarityLinInvMax.put(10l, 0.3f);

        expectedSimilarityLinInvAvg.put(1l, 0.366666667f);
        expectedSimilarityLinInvAvg.put(2l, 0.261904762f);
        expectedSimilarityLinInvAvg.put(3l, 0.155555556f);
        expectedSimilarityLinInvAvg.put(4l, 0.1f);
        expectedSimilarityLinInvAvg.put(5l, 0.427777778f);
        expectedSimilarityLinInvAvg.put(6l, 0.509090909f);
        expectedSimilarityLinInvAvg.put(7l, 0.3f);
        expectedSimilarityLinInvAvg.put(8l, 0.614285714f);
        expectedSimilarityLinInvAvg.put(9l, 0.34f);
        expectedSimilarityLinInvAvg.put(10l, 0.293333333f);

        // done

        expectedSimilarityTrivialCos.put(1l, 0.8987171f);
        expectedSimilarityTrivialCos.put(2l, 0.912870929f);
        expectedSimilarityTrivialCos.put(3l, 0.948683298f);
        expectedSimilarityTrivialCos.put(4l, 1f);
        expectedSimilarityTrivialCos.put(5l, 0.960987652f);
        expectedSimilarityTrivialCos.put(6l, 0.980580676f);
        expectedSimilarityTrivialCos.put(7l, 0.948683298f);
        expectedSimilarityTrivialCos.put(8l, 0.990867389f);
        expectedSimilarityTrivialCos.put(9l, 0.832050294f);
        expectedSimilarityTrivialCos.put(10l, 0.843274043f);

        expectedSimilarityTrivialAvg.put(1l, 0.35f);
        expectedSimilarityTrivialAvg.put(2l, 0.25f);
        expectedSimilarityTrivialAvg.put(3l, 0.15f);
        expectedSimilarityTrivialAvg.put(4l, 0.1f);
        expectedSimilarityTrivialAvg.put(5l, 0.43333333f);
        expectedSimilarityTrivialAvg.put(6l, 0.5f);
        expectedSimilarityTrivialAvg.put(7l, 0.3f);
        expectedSimilarityTrivialAvg.put(8l, 0.6f);
        expectedSimilarityTrivialAvg.put(9l, 0.3f);
        expectedSimilarityTrivialAvg.put(10l, 0.266666667f);

        expectedSimilarityTrivialMax.put(1l, 0.6f);
        expectedSimilarityTrivialMax.put(2l, 0.4f);
        expectedSimilarityTrivialMax.put(3l, 0.2f);
        expectedSimilarityTrivialMax.put(4l, 0.1f);
        expectedSimilarityTrivialMax.put(5l, 0.6f);
        expectedSimilarityTrivialMax.put(6l, 0.6f);
        expectedSimilarityTrivialMax.put(7l, 0.4f);
        expectedSimilarityTrivialMax.put(8l, 0.7f);
        expectedSimilarityTrivialMax.put(9l, 0.5f);
        expectedSimilarityTrivialMax.put(10l, 0.5f);

        for (String t : terms) {
            trivalWeights.put(t, 1f);
        }

        expectedLogInverseWeights.put("A", 0.510825624f);
        expectedLogInverseWeights.put("B", 0.693147181f);
        expectedLogInverseWeights.put("C", 1.203972804f);
        expectedLogInverseWeights.put("D", 0.693147181f);
        expectedLogInverseWeights.put("E", 0.916290732f);
        expectedLogInverseWeights.put("F", 0.916290732f);
        expectedLogInverseWeights.put("G", 2.302585093f);

        expectedLinearInverseWeights.put("A", 0.4f);
        expectedLinearInverseWeights.put("B", 0.5f);
        expectedLinearInverseWeights.put("C", 0.7f);
        expectedLinearInverseWeights.put("D", 0.5f);
        expectedLinearInverseWeights.put("E", 0.6f);
        expectedLinearInverseWeights.put("F", 0.6f);
        expectedLinearInverseWeights.put("G", 0.9f);

        exptectedTermCounts.put("A", 6);
        exptectedTermCounts.put("B", 5);
        exptectedTermCounts.put("C", 3);
        exptectedTermCounts.put("D", 5);
        exptectedTermCounts.put("E", 4);
        exptectedTermCounts.put("F", 4);
        exptectedTermCounts.put("G", 1);

        Assert.assertEquals(terms.length, trivalWeights.size());
        Assert.assertEquals(terms.length, expectedLogInverseWeights.size());
        Assert.assertEquals(terms.length, expectedLinearInverseWeights.size());
        Assert.assertEquals(terms.length, exptectedTermCounts.size());

    }

    private void initTermFrequencies() {

        termFrequencyComputer = new TermFrequencyComputer(getPersistence(), false);

        for (Message message : this.messages) {
            termFrequencyComputer.integrate(message);
        }

        assertTermCounts();
    }

    private void initUserModel() {
        UserModel userModel = getPersistence().getOrCreateUserModelByUser(
                "user1_" + UUID.randomUUID().toString());

        Collection<UserModelEntry> entries = new ArrayList<UserModelEntry>();
        entries.add(new UserModelEntry(userModel, new ScoredTerm(getPersistence().getOrCreateTerm(
                TermCategory.TERM, "A"), 0.1f)));
        entries.add(new UserModelEntry(userModel, new ScoredTerm(getPersistence().getOrCreateTerm(
                TermCategory.TERM, "B"), 0.2f)));
        entries.add(new UserModelEntry(userModel, new ScoredTerm(getPersistence().getOrCreateTerm(
                TermCategory.TERM, "C"), 0.3f)));
        entries.add(new UserModelEntry(userModel, new ScoredTerm(getPersistence().getOrCreateTerm(
                TermCategory.TERM, "D"), 0.4f)));
        entries.add(new UserModelEntry(userModel, new ScoredTerm(getPersistence().getOrCreateTerm(
                TermCategory.TERM, "E"), 0.5f)));
        entries.add(new UserModelEntry(userModel, new ScoredTerm(getPersistence().getOrCreateTerm(
                TermCategory.TERM, "F"), 0.6f)));
        entries.add(new UserModelEntry(userModel, new ScoredTerm(getPersistence().getOrCreateTerm(
                TermCategory.TERM, "G"), 0.7f)));

        for (UserModelEntry entry : entries) {
            userModelEntriesMap.put(entry.getScoredTerm().getTerm(), entry);

        }
    }

    @Test
    public void testLinerTermWeights() {
        testTermWeights(TermWeightStrategy.LINEAR_INVERSE_TERM_FREQUENCY,
                this.expectedLinearInverseWeights);
    }

    @Test
    public void testLogTermWeights() {
        testTermWeights(TermWeightStrategy.INVERSE_TERM_FREQUENCY, this.expectedLogInverseWeights);
    }

    @Test
    public void testSimilarityAvg() {

        computeMessageSimilarity(TermVectorSimilarityStrategy.AVG, TermWeightStrategy.TRIVIAL,
                this.expectedSimilarityTrivialAvg);
        computeMessageSimilarity(TermVectorSimilarityStrategy.AVG,
                TermWeightStrategy.INVERSE_TERM_FREQUENCY, this.expectedSimilarityLogInvAvg);
        computeMessageSimilarity(TermVectorSimilarityStrategy.AVG,
                TermWeightStrategy.LINEAR_INVERSE_TERM_FREQUENCY, this.expectedSimilarityLinInvAvg);

    }

    @Test
    public void testSimilarityCos() {
        computeMessageSimilarity(TermVectorSimilarityStrategy.COSINUS, TermWeightStrategy.TRIVIAL,
                this.expectedSimilarityTrivialCos);
        computeMessageSimilarity(TermVectorSimilarityStrategy.COSINUS,
                TermWeightStrategy.INVERSE_TERM_FREQUENCY, this.expectedSimilarityLogInvCos);
        computeMessageSimilarity(TermVectorSimilarityStrategy.COSINUS,
                TermWeightStrategy.LINEAR_INVERSE_TERM_FREQUENCY, this.expectedSimilarityLinInvCos);

    }

    @Test
    public void testSimilarityMax() {

        computeMessageSimilarity(TermVectorSimilarityStrategy.MAX, TermWeightStrategy.TRIVIAL,
                this.expectedSimilarityTrivialMax);
        computeMessageSimilarity(TermVectorSimilarityStrategy.MAX,
                TermWeightStrategy.INVERSE_TERM_FREQUENCY, this.expectedSimilarityLogInvMax);
        computeMessageSimilarity(TermVectorSimilarityStrategy.MAX,
                TermWeightStrategy.LINEAR_INVERSE_TERM_FREQUENCY, this.expectedSimilarityLinInvMax);

    }

    public void testTermWeights(TermWeightStrategy termWeightStrategy,
            Map<String, Float> expectedValues) {

        TermWeightComputer termWeightComputer = TermSimilarityWeightComputerFactory.getInstance()
                .createTermWeightComputer(termWeightStrategy, termFrequencyComputer);

        for (String term : terms) {
            Term t = getPersistence().getOrCreateTerm(TermCategory.TERM, term);
            float weight = termWeightComputer.determineTermWeight(null, t);
            float expected = expectedValues.get(term);
            Assert.assertEquals("Term: " + term + " " + termWeightStrategy, expected, weight,
                    EPSILON);
        }

    }

    @Test
    public void testTrivalTermWeights() {
        testTermWeights(TermWeightStrategy.TRIVIAL, this.trivalWeights);

    }
}
