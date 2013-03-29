package de.spektrumprojekt.aggregator.adapter.rss;

import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Test;

public class Base64EncodingTest {

    @Test
    public void test() throws UnsupportedEncodingException {
        testBaseAuthString("e1", "[", "ZTE6Ww==");
        testBaseAuthString("e2", "]", "ZTI6XQ==");
        testBaseAuthString("e3", "{", "ZTM6ew==");
        testBaseAuthString("e4", "}", "ZTQ6fQ==");

        testBaseAuthString("tlu", "test", "dGx1OnRlc3Q=");
        testBaseAuthString("weird", "°!\"§$%&/()=?", "d2VpcmQ6wrAhIsKnJCUmLygpPT8=");
        testBaseAuthString("weird2", "moreToCome;.-_öäü",
                "d2VpcmQyOm1vcmVUb0NvbWU7Li1fw7bDpMO8");
        testBaseAuthString("weird3", "[]{}", "d2VpcmQzOltde30=");
        testBaseAuthString("weird-er", "moreToCome;.-_\u00F6\u00E4\u00FC[]{}\\\u00df/",
                "d2VpcmQtZXI6bW9yZVRvQ29tZTsuLV/DtsOkw7xbXXt9XMOfLw==");
    }

    private void testBaseAuthString(String login, String password, String expectedBase64)
            throws UnsupportedEncodingException {
        String base64 = FeedAdapter.getBaseAuthenticationCredentials(login, password);

        Assert.assertEquals("Base64 Encoding for " + login, expectedBase64, base64);
    }
}
