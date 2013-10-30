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

    public enum FeatureType {
        BOOLEAN, NUMERIC, ORDINAL, NOMINAL;
    }

    private final String id;

    private final FeatureType featureType;

    /**
     * NULL Feature indicating no feature matched
     */
    public final static Feature NULL_FEATURE = new Feature("NullFeature", FeatureType.BOOLEAN);
    /**
     * The author feature
     */
    public final static Feature AUTHOR_FEATURE = new Feature("AuthorFeature", FeatureType.BOOLEAN);
    /**
     * The discussion root feature
     */
    public final static Feature DISCUSSION_ROOT_FEATURE = new Feature("DiscussionRootFeature",
            FeatureType.BOOLEAN);
    /**
     * The discussion participation feature
     */
    public final static Feature DISCUSSION_PARTICIPATION_FEATURE = new Feature(
            "DiscussionParticipationFeature", FeatureType.BOOLEAN);
    /**
     * The discussion mention feature
     */
    public final static Feature DISCUSSION_MENTION_FEATURE = new Feature(
            "DiscussionMentionFeature", FeatureType.BOOLEAN);

    public final static Feature ATTACHMENT_FEATURE = new Feature("AttachmentFeature",
            FeatureType.BOOLEAN);

    /**
     * The mention
     */
    public final static Feature MENTION_FEATURE = new Feature("MentionFeature", FeatureType.BOOLEAN);

    /**
     * The like
     */
    public final static Feature LIKE_FEATURE = new Feature("LikeFeature", FeatureType.BOOLEAN);

    /**
     * The mention
     */
    public final static Feature CONTENT_MATCH_FEATURE = new Feature("TermMatch",
            FeatureType.NUMERIC);

    public final static List<Feature> ALL_FEATURES;

    static {
        List<Feature> all = new ArrayList<Feature>();
        all.add(AUTHOR_FEATURE);
        all.add(ATTACHMENT_FEATURE);
        all.add(MENTION_FEATURE);
        all.add(LIKE_FEATURE);
        all.add(DISCUSSION_ROOT_FEATURE);
        all.add(DISCUSSION_PARTICIPATION_FEATURE);
        all.add(DISCUSSION_MENTION_FEATURE);
        all.add(CONTENT_MATCH_FEATURE);
        all.add(NULL_FEATURE);

        ALL_FEATURES = Collections.unmodifiableList(all);
    }

    public static String toString(Map<Feature, MessageFeature> features, String seperator,
            boolean includeFeatureId) {
        StringBuilder sb = new StringBuilder();
        String prefix = "";
        for (Feature feature : Feature.ALL_FEATURES) {
            MessageFeature mf = features.get(feature);
            float val = mf == null ? 0 : mf.getValue();

            sb.append(prefix);
            if (includeFeatureId) {
                sb.append(feature.getId() + ": ");
            }
            sb.append(val);
            prefix = seperator;
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
    public Feature(String id, FeatureType featureType) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        if (featureType == null) {
            throw new IllegalArgumentException("featureType cannot be null");
        }
        this.id = id;
        this.featureType = featureType;
    }

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
        if (featureType != other.featureType) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public FeatureType getFeatureType() {
        return featureType;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (featureType == null ? 0 : featureType.hashCode());
        result = prime * result + (id == null ? 0 : id.hashCode());
        return result;
    }

    public boolean isBooleanFeature() {
        return FeatureType.BOOLEAN.equals(this.featureType);
    }

    public boolean isNominalFeature() {
        return FeatureType.NOMINAL.equals(this.featureType);
    }

    public boolean isNumericFeature() {
        return FeatureType.NUMERIC.equals(this.featureType);
    }

    public boolean isOrdinalFeature() {
        return FeatureType.ORDINAL.equals(this.featureType);
    }

    @Override
    public String toString() {
        return "Feature [id=" + id + ", featureType=" + featureType + "]";
    }
}
