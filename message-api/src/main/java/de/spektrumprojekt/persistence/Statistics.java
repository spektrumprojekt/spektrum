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

package de.spektrumprojekt.persistence;

/**
 * Statistics holding the counts of some entities. Value of -1 indicate that the value has not been
 * computed (e.g. it is not easily computable for the persistence implementation used).
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class Statistics {

    private long subscriptionCount;

    private long messageCount;
    private long messageRankCount;

    private long scoredTermCount;

    private long termCount;

    private long userCount;

    private long userModelCount;

    private long userModelEntryCount;
    private long userModelEntryTimeBinCount;

    public long getMessageCount() {
        return messageCount;
    }

    public long getMessageRankCount() {
        return messageRankCount;
    }

    public long getScoredTermCount() {
        return scoredTermCount;
    }

    public long getSubscriptionCount() {
        return subscriptionCount;
    }

    public long getTermCount() {
        return termCount;
    }

    public long getUserCount() {
        return userCount;
    }

    public long getUserModelCount() {
        return userModelCount;
    }

    public long getUserModelEntryCount() {
        return userModelEntryCount;
    }

    public long getUserModelEntryTimeBinCount() {
        return userModelEntryTimeBinCount;
    }

    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    public void setMessageRankCount(long messageRankCount) {
        this.messageRankCount = messageRankCount;
    }

    public void setScoredTermCount(long scoredTermCount) {
        this.scoredTermCount = scoredTermCount;
    }

    public void setSubscriptionCount(long subscriptionCount) {
        this.subscriptionCount = subscriptionCount;
    }

    public void setTermCount(long termCount) {
        this.termCount = termCount;
    }

    public void setUserCount(long userCount) {
        this.userCount = userCount;
    }

    public void setUserModelCount(long userModelCount) {
        this.userModelCount = userModelCount;
    }

    public void setUserModelEntryCount(long userModelEntryCount) {
        this.userModelEntryCount = userModelEntryCount;
    }

    public void setUserModelEntryTimeBinCount(long userModelEntryTimeBinCount) {
        this.userModelEntryTimeBinCount = userModelEntryTimeBinCount;
    }

}
