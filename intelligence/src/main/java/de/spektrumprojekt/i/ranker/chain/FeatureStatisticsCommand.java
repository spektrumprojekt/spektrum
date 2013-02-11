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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.ranker.chain.features.Feature;

/**
 * A command that will create learning message based on the ranked message
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class FeatureStatisticsCommand implements Command<UserSpecificMessageFeatureContext> {

    private final Map<Feature, Integer> featureCounts = new HashMap<Feature, Integer>();

    private int contextCount;

    /**
     * 
     * @param persistence
     *            the persistence to use
     */
    public FeatureStatisticsCommand() {
        for (Feature f : Feature.ALL_FEATURES) {
            this.featureCounts.put(f, 0);
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
            sb.append(this.featureCounts.get(f));

            int percent = 0;
            if (contextCount > 0) {
                percent = 100 * this.featureCounts.get(f) / contextCount;
            }
            sb.append(" " + percent + "%");
            values.add(sb.toString());
        }
        values.add("contextCount: " + contextCount);
        return values;
    }

    public Map<Feature, Integer> getFeatureCounts() {
        return Collections.unmodifiableMap(featureCounts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {

        contextCount++;
        for (MessageFeature feature : context.getFeatures().values()) {
            if (feature.getValue() == 1) {
                Integer count = this.featureCounts.get(feature.getFeatureId());
                this.featureCounts.put(feature.getFeatureId(), count + 1);
            }
        }

    }

}
