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

package de.spektrumprojekt.aggregator.adapter.twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.configuration.ConfigurationException;

import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterAuthenticator {

    public static void main(String[] args) throws TwitterException, IOException,
            ConfigurationException {

        AggregatorConfiguration config = AggregatorConfiguration.loadXmlConfig();

        TwitterAuthenticator authenticator = new TwitterAuthenticator(
                config.getTwitterConsumerKey(), config.getTwitterConsumerSecret());
        RequestToken requestToken = authenticator.getRequestToken();
        System.out.println("Open the following URL and grant access to your account:");
        System.out.println(requestToken.getAuthenticationURL());

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String pin = br.readLine();

        AccessToken accessToken = authenticator.getAccessToken(requestToken, pin);
        System.out.println("Access token: " + accessToken.getToken());
        System.out.println("Access token secret: " + accessToken.getTokenSecret());
    }

    private final Twitter twitter;

    public TwitterAuthenticator(String consumerKey, String consumerSecret) {
        twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
    }

    public AccessToken getAccessToken(RequestToken requestToken, String pin)
            throws TwitterException {
        return twitter.getOAuthAccessToken(requestToken, pin);
    }

    public RequestToken getRequestToken() throws TwitterException {
        return twitter.getOAuthRequestToken();
    }

}
