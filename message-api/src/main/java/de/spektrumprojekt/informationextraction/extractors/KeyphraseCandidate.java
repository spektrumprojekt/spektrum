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

package de.spektrumprojekt.informationextraction.extractors;

import org.apache.commons.collections.bag.HashBag;


public class KeyphraseCandidate {

    private String stemmedValue;
    private org.apache.commons.collections.Bag unstemmedValues;
    private int count;

    public KeyphraseCandidate(String stemmedValue, String unstemmedValue, int count) {
        this.stemmedValue = stemmedValue;
        this.unstemmedValues = new HashBag();
        this.unstemmedValues.add(unstemmedValue);
        this.count = count;
    }

    public void add(String unstemmedValue, int count) {
        this.unstemmedValues.add(unstemmedValue);
        this.count += count;
    }

    public void decrementCount(int by) {
        count -= by;
    }

    public int getCount() {
        return count;
    }

    public String getMostCommonUnstemmedValue() {
        return (String) BagHelper.getHighestItem(unstemmedValues);
    }

    public String getShortestUnstemmedValue() {
        String ret = null;
        for (Object unstemmedValueObj : unstemmedValues) {
            String unstemmedValue = (String) unstemmedValueObj;
            if (ret == null) {
                ret = unstemmedValue;
                continue;
            }
            if (ret.length() > unstemmedValue.length()) {
                ret = unstemmedValue;
            }
        }
        return ret;
    }

    public String getStemmedValue() {
        return stemmedValue;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(stemmedValue).append(":").append(count);
        return stringBuilder.toString();
    }

}
