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
        testBaseAuthString("weird-er", "moreToCome;.-_öäü[]{}\\ß/",
                "d2VpcmQtZXI6bW9yZVRvQ29tZTsuLV/DtsOkw7xbXXt9XMOfLw==");
    }

    private void testBaseAuthString(String login, String password, String expectedBase64)
            throws UnsupportedEncodingException {
        String base64 = FeedAdapter.getBaseAuthenticationCredentials(login, password);

        Assert.assertEquals("Base64 Encoding for " + login, expectedBase64, base64);
    }

}
