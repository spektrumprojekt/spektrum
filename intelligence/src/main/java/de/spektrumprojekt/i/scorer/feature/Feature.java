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

package de.spektrumprojekt.i.scorer.feature;

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
     * The author feature
     */
    public final static Feature AUTHOR_FEATURE = new Feature("Author", FeatureType.BOOLEAN);
    /**
     * The discussion root feature
     */
    public final static Feature MESSAGE_ROOT_FEATURE = new Feature("MessageRoot",
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

    public final static Feature DISCUSSION_NO_MENTION_FEATURE = new Feature(
            "DiscussionNoMentionFeature", FeatureType.BOOLEAN);

    public final static Feature DISCUSSION_NO_PARTICIPATION_FEATURE = new Feature(
            "DiscussionNoParticipationFeature", FeatureType.BOOLEAN);

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
    public final static Feature CONTENT_MATCH_FEATURE = new Feature("ContentMatch",
            FeatureType.NUMERIC);

    public final static Feature COLLABORATION_MATCH_FEATURE = new Feature("CollaborationMatch",
            FeatureType.NUMERIC);

    public final static Feature MESSAGE_TEXT_LENGTH_FEATURE = new Feature("messageTextLength",
            FeatureType.NUMERIC);
    public final static Feature CLEANED_TEXT_LENGTH_FEATURE = new Feature("cleanedTextLength",
            FeatureType.NUMERIC);
    public final static Feature NUM_TERMS_FEATURE = new Feature("numTerms", FeatureType.NUMERIC);
    public final static Feature USER_TO_SIM_FEATURE = new Feature("userToSim", FeatureType.NUMERIC);
    public final static Feature USER_FROM_SIM_FEATURE = new Feature("userFromSim",
            FeatureType.NUMERIC);
    public final static Feature NUM_MENTIONS_FEATURE = new Feature("numMentions",
            FeatureType.NUMERIC);
    public final static Feature NUM_LIKES_FEATURE = new Feature("numLikes", FeatureType.NUMERIC);
    public final static Feature NUM_TAGS_FEATURE = new Feature("numTags", FeatureType.NUMERIC);
    public final static Feature NUM_DISCUSSION_FEATURE = new Feature("numDiscussion",
            FeatureType.NUMERIC);
    public final static Feature NUM_AUTHORS_FEATURE = new Feature("numAuthors", FeatureType.NUMERIC);
    public final static Feature NUM_DISCUSSION_MENTIONS_FEATURE = new Feature(
            "numDiscussionMentions", FeatureType.NUMERIC);
    public final static Feature NUM_DISCUSSION_TAGS_FEATURE = new Feature("numDiscussionTags",
            FeatureType.NUMERIC);
    public final static Feature NUM_ATTACHMENTS_FEATURE = new Feature("numAttachments",
            FeatureType.NUMERIC);
    public final static Feature NUM_IMAGE_ATTACHMENTS_FEATURE = new Feature("numImageAttachments",
            FeatureType.NUMERIC);
    public final static Feature NUM_NONE_IMAGE_ATTACHMENTS_FEATURE = new Feature(
            "numNoneImageAttachments", FeatureType.NUMERIC);

    public final static List<Feature> ALL_FEATURES;

    static {
        List<Feature> all = new ArrayList<Feature>();
        all.add(AUTHOR_FEATURE);
        all.add(ATTACHMENT_FEATURE);
        all.add(MENTION_FEATURE);
        all.add(LIKE_FEATURE);
        all.add(MESSAGE_ROOT_FEATURE);
        all.add(DISCUSSION_PARTICIPATION_FEATURE);
        all.add(DISCUSSION_MENTION_FEATURE);
        all.add(DISCUSSION_NO_PARTICIPATION_FEATURE);
        all.add(DISCUSSION_NO_MENTION_FEATURE);
        all.add(CONTENT_MATCH_FEATURE);
        all.add(MESSAGE_TEXT_LENGTH_FEATURE);
        all.add(CLEANED_TEXT_LENGTH_FEATURE);
        all.add(NUM_TERMS_FEATURE);
        all.add(USER_TO_SIM_FEATURE);
        all.add(USER_FROM_SIM_FEATURE);
        all.add(NUM_MENTIONS_FEATURE);
        all.add(NUM_LIKES_FEATURE);
        all.add(NUM_TAGS_FEATURE);
        all.add(NUM_DISCUSSION_FEATURE);
        all.add(NUM_AUTHORS_FEATURE);
        all.add(NUM_DISCUSSION_MENTIONS_FEATURE);
        all.add(NUM_DISCUSSION_TAGS_FEATURE);
        all.add(NUM_ATTACHMENTS_FEATURE);
        all.add(NUM_IMAGE_ATTACHMENTS_FEATURE);
        all.add(NUM_NONE_IMAGE_ATTACHMENTS_FEATURE);

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
