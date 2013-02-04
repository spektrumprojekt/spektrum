package de.spektrumprojekt.datamodel.message;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.Assert;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import de.spektrumprojekt.datamodel.message.MessageRelation.MessageRelationType;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class MessageRelationtTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    public void jsonTest() throws JsonGenerationException, JsonMappingException, IOException {
        MessageRelation mr = new MessageRelation(MessageRelationType.DISCUSSION, new String[] {
                "id1", "id2", "id3" });

        String json = om.writeValueAsString(mr);

        MessageRelation mr2 = om.readValue(json, MessageRelation.class);

        Assert.assertNotNull(mr2);

        Assert.assertEquals(mr.getGlobalId(), mr2.getGlobalId());

        Assert.assertTrue(Arrays.equals(mr.getRelatedMessageGlobalIds(),
                mr2.getRelatedMessageGlobalIds()));

    }
}
