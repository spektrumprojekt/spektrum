package de.spektrumprojekt.i.evaluation.rank;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import de.spektrumprojekt.datamodel.message.InteractionLevel;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.i.evaluation.measure.EvaluatorDataPoint;
import de.spektrumprojekt.i.scorer.feature.Feature;

public class InternalMessageRank {

    private static final String N_A = "n/a";

    public static String toHeaderString() {
        return StringUtils.join(new String[] {
                "messageId",
                "messagePublicationDate",
                "userId",
                "isAuthor",
                "interactionLevel",
                "score",
                "order"
        }, " ");
    }

    public static String toParseableString(UserMessageScore messageScore, Message message) {
        return StringUtils.join(
                new String[] { messageScore.getMessageGlobalId(),
                        message.getPublicationDate().getTime() + "",
                        messageScore.getUserGlobalId(),
                        messageScore.getMessageGlobalId().equals(message.getAuthorGlobalId()) + "",
                        messageScore.getInteractionLevel().getNumberValue() + "",
                        "" + messageScore.getScore()
                }, " ");
    }

    private final String messageId;

    private final String userId;

    private final double score;

    private final InteractionLevel interactionLevel;

    private final Date publicationDate;

    private final Boolean author;

    private int orderRank;

    public InternalMessageRank(EvaluatorDataPoint dataPoint) {
        this.messageId = dataPoint.getMessageGlobalId();
        this.publicationDate = dataPoint.getPublicationDateAsDate();
        this.userId = dataPoint.getUserGlobalId();
        if (dataPoint.getFeatures() != null) {
            this.author = dataPoint.getFeatures().getFeatureValue(Feature.AUTHOR_FEATURE) >= 0.99f;
            this.interactionLevel = dataPoint.getFeatures().getInteractionLevel();
        } else {
            this.author = null;
            this.interactionLevel = null;
        }
        this.score = dataPoint.getComputed();

    }

    public InternalMessageRank(String row) {
        String[] values = row.split(" ");
        messageId = values[0];
        publicationDate = new Date(Long.parseLong(values[1]));
        userId = values[2];
        String a = values[3];
        if (a.equals(N_A)) {
            author = null;
        } else {
            author = Boolean.parseBoolean(values[3]);
        }
        String il = values[4];
        if (N_A.equals(il)) {
            interactionLevel = null;
        } else {
            interactionLevel = InteractionLevel.fromNumberValue(Integer.parseInt(il));
        }

        score = Double.parseDouble(values[5]);
    }

    public InteractionLevel getInteractionLevel() {
        return interactionLevel;
    }

    public String getMessageId() {
        return messageId;
    }

    public int getOrderRank() {
        return orderRank;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public double getScore() {
        return score;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isAuthor() {
        return author;
    }

    public void setOrderRank(int orderRank) {
        this.orderRank = orderRank;
    }

    public String toParseableString() {
        return StringUtils.join(
                new String[] {
                        this.messageId,
                        this.publicationDate.getTime() + "",
                        this.userId,
                        this.author == null ? N_A : this.author.toString(),
                        this.interactionLevel == null ? N_A : this.interactionLevel
                                .getNumberValue() + "",
                        "" + this.getScore(),
                        "" + this.getOrderRank()

                }, " ");
    }
}