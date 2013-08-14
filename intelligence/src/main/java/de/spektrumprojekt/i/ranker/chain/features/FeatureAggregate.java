package de.spektrumprojekt.i.ranker.chain.features;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.spektrumprojekt.datamodel.message.InteractionLevel;
import de.spektrumprojekt.i.datamodel.MessageFeature;

public class FeatureAggregate {

    public static int getLength() {
        return Feature.ALL_FEATURES.size() + 13;
    }

    public static String getSimpleFeaturesHeader() {
        return StringUtils.join(new String[] {
                Feature.toStringHeader(),
                "MessageTextLength",
                "CleanedTextLength",
                "TermCount",
                "UserSimTo",
                "UserSimFrom",
                "NumberOfMentions",
                "NumberOfLikes",
                "NumberOfTags",
                "NumnerOfDiscussion",
                "DiscussionParticipaters",
                "DiscussionMentions",
                "DiscussionTags",
                "InteractionLevel"
        }, " ");
    }

    public int messageTextLength;
    public int cleanedTextLength;
    public int numTerms;
    public double userToSim;
    public double userFromSim;
    public int numMentions;
    public int numLikes;
    public int numTags;
    public int numDiscussion;
    public int numAuthors;
    public int numDiscussionMentions;
    public int numDiscussionTags;
    public InteractionLevel interactionLevel;

    public final Map<Feature, MessageFeature> features = new HashMap<Feature, MessageFeature>();

    public FeatureAggregate() {
    }

    public FeatureAggregate(String[] stats) {
        if (stats.length < getLength()) {

            throw new IllegalArgumentException("minLength is " + getLength() + " but only got "
                    + stats.length + " stats=" + StringUtils.join(stats, " "));

        }
        int index = parseFeatures(stats);

        messageTextLength = Integer.parseInt(stats[index++]);
        cleanedTextLength = Integer.parseInt(stats[index++]);
        numTerms = Integer.parseInt(stats[index++]);
        userToSim = Double.parseDouble(stats[index++]);
        userFromSim = Double.parseDouble(stats[index++]);
        numMentions = Integer.parseInt(stats[index++]);
        numLikes = Integer.parseInt(stats[index++]);
        numTags = Integer.parseInt(stats[index++]);
        numDiscussion = Integer.parseInt(stats[index++]);
        numAuthors = Integer.parseInt(stats[index++]);
        numDiscussionMentions = Integer.parseInt(stats[index++]);
        numDiscussionTags = Integer.parseInt(stats[index++]);
        interactionLevel = InteractionLevel.fromNumberValue(Integer.parseInt(stats[index++]));
        if (interactionLevel == null) {
            throw new IllegalArgumentException("Error reading rank. Invalid interactionLevel: "
                    + stats[index - 1] + " Line was: " + stats);
        }
    }

    private int parseFeatures(String[] stats) {
        int index = 0;
        for (Feature feature : Feature.ALL_FEATURES) {
            float num = Float.parseFloat(stats[index++]);
            MessageFeature mf = new MessageFeature(feature);
            mf.setValue(num);
            this.features.put(feature, mf);

        }
        return index;
    }

    public String toParseableString() {
        String featuresStr = Feature.toString(features, " ", false);
        return StringUtils.join(
                new String[] {
                        featuresStr,
                        "" + messageTextLength,
                        "" + cleanedTextLength,
                        "" + numTerms,
                        "" + userToSim,
                        "" + userFromSim,
                        "" + numMentions,
                        "" + numLikes,
                        "" + numTags,
                        "" + numDiscussion,
                        "" + numAuthors,
                        "" + numDiscussionMentions,
                        "" + numDiscussionTags,
                        "" + interactionLevel.getNumberValue()
                }, " ");
    }

    @Override
    public String toString() {
        return this.toParseableString();
    }

}
