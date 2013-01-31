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

package de.spektrumprojekt.i.ranker.chain.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.spektrumprojekt.i.datamodel.MessageFeature;

/**
 * A feature definition
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class Feature {

    private final String id;

    /**
     * The author feature
     */
    public final static Feature AUTHOR_FEATURE = new Feature("AuthorFeature");

    /**
     * The discussion root feature
     */
    public final static Feature DISCUSSION_ROOT_FEATURE = new Feature("DiscussionRootFeature");

    /**
     * The discussion participation feature
     */
    public final static Feature DISCUSSION_PARTICIPATION_FEATURE = new Feature(
            "DiscussionParticipationFeature");

    /**
     * The discussion mention feature
     */
    public final static Feature DISCUSSION_MENTION_FEATURE = new Feature(
            "DiscussionMentionFeature");

    /**
     * The mention
     */
    public final static Feature MENTION_FEATURE = new Feature(
            "MentionFeature");

    /**
     * The mention
     */
    public final static Feature TERM_MATCH_FEATURE = new Feature(
            "TermMatch");

    public final static List<Feature> ALL_FEATURES;

    static {
        List<Feature> all = new ArrayList<Feature>();
        all.add(AUTHOR_FEATURE);
        all.add(MENTION_FEATURE);
        all.add(DISCUSSION_ROOT_FEATURE);
        all.add(DISCUSSION_PARTICIPATION_FEATURE);
        all.add(DISCUSSION_MENTION_FEATURE);
        all.add(TERM_MATCH_FEATURE);

        ALL_FEATURES = Collections.unmodifiableList(all);
    }

    public static String toString(Map<Feature, MessageFeature> features) {
        StringBuilder sb = new StringBuilder();
        for (Feature feature : Feature.ALL_FEATURES) {
            MessageFeature mf = features.get(feature);
            float val = mf == null ? 0 : mf.getValue();
            sb.append(val + " ");
        }
        return sb.toString();
    }

    public static String toStringHeader() {
        StringBuilder sb = new StringBuilder();
        for (Feature feature : Feature.ALL_FEATURES) {
            sb.append(feature.getId() + " ");
        }
        return sb.toString();

    }

    /**
     * 
     * @param id
     *            the feature of the id
     */
    private Feature(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Feature other = (Feature) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (id == null ? 0 : id.hashCode());
        return result;
    }
}
