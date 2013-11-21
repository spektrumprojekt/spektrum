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

package de.spektrumprojekt.i.ranker.chain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.ranker.feature.Feature;

/**
 * A command that will create learning message based on the ranked message
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class FeatureStatisticsCommand implements Command<UserSpecificMessageFeatureContext> {

    private class FeatureStat {
        public int ones;
        public int zeroPointFive;
        public int greaterZero;
        public int exists;

        private String getPercentString(int val, int overall) {
            int percent = overall == 0 ? 0 : 100 * val / overall;
            return val + " (" + percent + "%) ";
        }

        @Override
        public String toString() {
            return " ==1: " + ones + " >0.5: " + zeroPointFive + " >0: " + greaterZero
                    + " exists: " + exists;
        }

        public String toString(int overall) {
            return " ==1: " + getPercentString(ones, overall) + " >=0.5: "
                    + getPercentString(zeroPointFive, overall) + " >0: "
                    + getPercentString(greaterZero, overall) + " exists: "
                    + getPercentString(exists, overall);
        }
    }

    private final Map<Feature, FeatureStat> featureCounts = new HashMap<Feature, FeatureStat>();

    private int contextCount;

    /**
     * 
     * @param persistence
     *            the persistence to use
     */
    public FeatureStatisticsCommand() {
        for (Feature f : Feature.ALL_FEATURES) {
            this.featureCounts.put(f, new FeatureStat());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();

    }

    public long getContextCount() {
        return contextCount;
    }

    public List<String> getFeatureCountAsString() {
        List<String> values = new ArrayList<String>();
        for (Feature f : Feature.ALL_FEATURES) {
            StringBuilder sb = new StringBuilder();
            sb.append(f.getId() + ": ");
            sb.append(this.featureCounts.get(f).toString(contextCount));

            values.add(sb.toString());
        }
        values.add("contextCount: " + contextCount);
        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {

        contextCount++;

        for (MessageFeature feature : context.getFeatures().values()) {
            FeatureStat stat = this.featureCounts.get(feature.getFeatureId());
            stat.exists++;

            if (feature.getValue() == 1) {
                stat.ones++;
            }
            if (feature.getValue() >= 0.5) {
                stat.zeroPointFive++;
            }
            if (feature.getValue() > 0) {
                stat.greaterZero++;
            }
        }

    }
}
