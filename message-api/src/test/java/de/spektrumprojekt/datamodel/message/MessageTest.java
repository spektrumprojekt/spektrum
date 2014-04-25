package de.spektrumprojekt.datamodel.message;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import junit.framework.Assert;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import de.spektrumprojekt.communication.transfer.ScorerCommunicationMessage;
import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.MessageRelation.MessageRelationType;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.helper.MessageHelper;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class MessageTest {

    private static final String CONTENT_HTML = "<p>I want a cookie. <b>Now</b>.</p>";
    private static final String CONTENT_PLAIN = "I want a cookie. Now.";
    private final ObjectMapper om = new ObjectMapper();

    @Test
    public void testJsonMessage() throws JsonGenerationException, JsonMappingException, IOException {

        Message message = new Message(MessageType.CONTENT, StatusType.OK, "sourceId", new Date());

        MessagePart mp1 = new MessagePart(MimeType.TEXT_PLAIN, CONTENT_PLAIN);
        MessagePart mp2 = new MessagePart(MimeType.TEXT_HTML, CONTENT_HTML);

        Term t = new Term(TermCategory.TERM, "cookie");
        ScoredTerm st = new ScoredTerm(t, 1f);
        mp1.addScoredTerm(st);
        message.addMessagePart(mp1);
        message.addMessagePart(mp2);

        Property prop = MessageHelper.createMentionProperty(new String[] { "userId1", "userId2" });
        message.addProperty(prop);

        String json = om.writeValueAsString(message);
        Assert.assertNotNull(json);

        Message newMessage = om.readValue(json, Message.class);

        Assert.assertNotNull(newMessage);

        Assert.assertEquals(message.getGlobalId(), newMessage.getGlobalId());
        Assert.assertEquals(message.getSourceGlobalId(), newMessage.getSourceGlobalId());
        Assert.assertEquals(message.getMessageType(), newMessage.getMessageType());

        Assert.assertEquals(1, newMessage.getProperties().size());
        Collection<String> newMentions = MessageHelper.getMentions(newMessage);

        Assert.assertEquals(2, newMentions.size());
        Assert.assertTrue(newMentions.contains("userId1"));
        Assert.assertTrue(newMentions.contains("userId2"));

        MessagePart newMp1 = null, newMp2 = null;
        Assert.assertEquals(2, newMessage.getMessageParts().size());
        for (MessagePart mp : newMessage.getMessageParts()) {
            if (mp.isMimeType(MimeType.TEXT_PLAIN)) {
                newMp1 = mp;
            }
            if (mp.isMimeType(MimeType.TEXT_HTML)) {
                newMp2 = mp;
            }
        }
        Assert.assertNotNull(newMp1);
        Assert.assertNotNull(newMp2);

        Assert.assertEquals(CONTENT_PLAIN, newMp1.getContent());
        Assert.assertEquals(CONTENT_HTML, newMp2.getContent());

        Assert.assertEquals(1, newMp1.getScoredTerms().size());

        ScoredTerm newSt = newMp1.getScoredTerms().iterator().next();
        Assert.assertEquals(st.getGlobalId(), newSt.getGlobalId());
        Assert.assertEquals(st.getWeight(), newSt.getWeight());
        Assert.assertEquals(st.getTerm().getValue(), newSt.getTerm().getValue());

    }

    @Test
    public void testJsonScorerCommunicationMessage() throws JsonGenerationException,
            JsonMappingException, IOException {
        Message message = new Message(MessageType.CONTENT, StatusType.OK, "sourceId", new Date());

        ScorerCommunicationMessage scm = new ScorerCommunicationMessage(message);
        scm.setNoScoringOnlyLearning(true);
        scm.setSubscriptionGlobalIds(new String[] { "sub1", "sub2" });
        scm.setUserGlobalIdsToScoreFor(new String[] { "user1", "user2" });
        scm.setMessageRelation(new MessageRelation(MessageRelationType.DISCUSSION,
                new String[] { "m1" }));

        String json = om.writeValueAsString(scm);
        Assert.assertNotNull(json);

        ScorerCommunicationMessage newScm = om.readValue(json, ScorerCommunicationMessage.class);
        Assert.assertNotNull(newScm);
        Assert.assertEquals(scm.getMessage().getGlobalId(), newScm.getMessage().getGlobalId());
        Assert.assertEquals(scm.getMessageRelation().getGlobalId(), newScm.getMessageRelation()
                .getGlobalId());
        Assert.assertTrue(newScm.isNoScoringOnlyLearning());
        Assert.assertTrue(Arrays.equals(scm.getSubscriptionGlobalIds(),
                newScm.getSubscriptionGlobalIds()));
        Assert.assertTrue(Arrays.equals(scm.getUserGlobalIdsToScoreFor(),
                newScm.getUserGlobalIdsToScoreFor()));
    }
}
