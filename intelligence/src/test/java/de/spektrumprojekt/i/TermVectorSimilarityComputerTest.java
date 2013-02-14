package de.spektrumprojekt.i;

import java.util.ArrayList;
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

    private Map<String, Float> logInverseWeights = new HashMap<String, Float>();

    private Map<String, Float> linearInverseWeights = new HashMap<String, Float>();

    private Map<String, Integer> exptectedTermCounts = new HashMap<String, Integer>();

    private void assertTermCounts() {
        for (Entry<String, Integer> entry : this.exptectedTermCounts.entrySet()) {
            Term term = getPersistence().getOrCreateTerm(TermCategory.TERM, entry.getKey());

            Assert.assertNotNull(entry.getKey(), term);
            Assert.assertEquals(entry.getKey(), entry.getValue().intValue(), term.getCount());
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
        messages.add(createMessage(4l, "A "));
        messages.add(createMessage(5l, "C D E F"));
        messages.add(createMessage(6l, "D F"));
        messages.add(createMessage(7l, "B D"));
        messages.add(createMessage(8l, "E F G"));
        messages.add(createMessage(9l, "A E"));
        messages.add(createMessage(10l, "A B"));

    }

    @Before
    public void init() throws ConfigurationException {

        super.setupPersistence();

        this.initExpectedValues();

        this.createMessages();

        this.initTermFrequencies();

    }

    private void initExpectedValues() {
        for (String t : terms) {
            trivalWeights.put(t, 1f);
        }

        // TODO
        logInverseWeights.put("A", 0.1f);
        logInverseWeights.put("B", 0.1f);
        logInverseWeights.put("C", 0.1f);
        logInverseWeights.put("D", 0.1f);
        logInverseWeights.put("E", 0.1f);
        logInverseWeights.put("F", 0.1f);
        logInverseWeights.put("G", 0.1f);

        // TODO
        linearInverseWeights.put("A", 0.1f);
        linearInverseWeights.put("B", 0.1f);
        linearInverseWeights.put("C", 0.1f);
        linearInverseWeights.put("D", 0.1f);
        linearInverseWeights.put("E", 0.1f);
        linearInverseWeights.put("F", 0.1f);
        linearInverseWeights.put("G", 0.1f);

        exptectedTermCounts.put("A", 6);
        exptectedTermCounts.put("B", 5);
        exptectedTermCounts.put("C", 3);
        exptectedTermCounts.put("D", 5);
        exptectedTermCounts.put("E", 4);
        exptectedTermCounts.put("F", 4);
        exptectedTermCounts.put("G", 1);

        Assert.assertEquals(terms.length, trivalWeights.size());
        Assert.assertEquals(terms.length, logInverseWeights.size());
        Assert.assertEquals(terms.length, linearInverseWeights.size());
        Assert.assertEquals(terms.length, exptectedTermCounts.size());

    }

    private void initTermFrequencies() {

        termFrequencyComputer = new TermFrequencyComputer(getPersistence(), false);

        for (Message message : this.messages) {
            termFrequencyComputer.integrate(message);
        }

        assertTermCounts();
    }

    @Test
    public void testSimilarity() {
        // TODO
        TermVectorSimilarityComputer termVectorSimilarityComputer;

        termVectorSimilarityComputer = TermSimilarityWeightComputerFactory.getInstance()
                .createTermVectorSimilarityComputer(
                        TermVectorSimilarityStrategy.MAX,
                        TermWeightStrategy.TRIVIAL,
                        termFrequencyComputer);

        // termVectorSimilarityComputer.getSimilarity(null, relevantEntries, terms);

    }

    @Test
    public void testTermWeights() {
        testTermWeights(TermWeightStrategy.TRIVIAL, this.trivalWeights);
        // TODO
        // testTermWeights(TermWeightStrategy.INVERSE_TERM_FREQUENCY, this.logInverseWeights);
        // testTermWeights(TermWeightStrategy.LINEAR_INVERSE_TERM_FREQUENCY,
        // this.linearInverseWeights);
    }

    public void testTermWeights(TermWeightStrategy termWeightStrategy,
            Map<String, Float> expectedValues) {

        TermWeightComputer termWeightComputer = TermSimilarityWeightComputerFactory.getInstance()
                .createTermWeightComputer(termWeightStrategy, termFrequencyComputer);

        for (String term : terms) {
            Term t = getPersistence().getOrCreateTerm(TermCategory.TERM, term);
            float weight = termWeightComputer.determineTermWeight(null, t);
            float expected = expectedValues.get(term);
            Assert.assertEquals("Term: " + term + " " + termWeightStrategy, expected, weight);
        }

    }
}
