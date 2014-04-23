package de.spektrumprojekt.i.evaluation.measure;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.output.SpektrumParseableElement;
import de.spektrumprojekt.i.ranker.feature.FeatureAggregate;

/**
 * This is a tupel of the message id, the user id, the computed score and goal/target score, and
 * some additional information. Hence there are only data points for messages and user pairs where a
 * target rating exists
 * 
 */
public class EvaluatorDataPoint implements SpektrumParseableElement {

    public static String getColumnHeaders() {
        return StringUtils.join(new String[] {
                "computed",
                "goal_rank",
                "message_global_id",
                "timeBin",
                "message_publication_date",
                "user_global_id",
                "basedOnAdaptedTerms",
                FeatureAggregate.getSimpleFeaturesHeader()
        }, " ");
    }

    public static String getFileDescription() {
        return "The evaluator file contains an entry with a goal rating (of the test rating set) "
                + "and the computed one. The values might be filtered (e.g. only top ranks). "
                + "Use " + EvaluatorOutput.class.getSimpleName() + " to read and process it.";
    }

    private double computed;

    private double target;
    private boolean unavailable;
    private String messageGlobalId;
    private int timeBin;
    private String userGlobalId;
    private String publicationDate;
    private FeatureAggregate features;

    private final static Logger LOGGER = LoggerFactory.getLogger(EvaluatorDataPoint.class);

    private boolean basedOnAdaptedTerms;

    // public String someId;
    public EvaluatorDataPoint() {
    }

    public EvaluatorDataPoint(double computed, double target) {
        this.computed = computed;
        this.target = target;
    }

    public EvaluatorDataPoint(String line) {
        String[] data = line.split("\\s+");

        try {
            computed = Double.parseDouble(data[0]);
        } catch (Exception e) {
            unavailable = true;
        }
        target = Double.parseDouble(data[1]);
        messageGlobalId = data[2];
        timeBin = Integer.parseInt(data[3]);
        publicationDate = data[4];
        userGlobalId = data[5];
        basedOnAdaptedTerms = Boolean.parseBoolean(data[6]);
        // try {
        String[] stats = Arrays.copyOfRange(data, 7, data.length);
        if (stats.length > 0) {
            features = new FeatureAggregate(stats);
        } else {
            LOGGER.warn("Did not got any stats, will ignore it");
        }
        // } catch (Exception e) {
        // features = null;
        // }
        if (!unavailable && (computed < 0 || computed > 1)) {
            throw new IllegalArgumentException("computed must be in [0,1] but is " + computed
                    + "! line: " + line);
        }
        if (target < 0 || target > 1) {
            throw new IllegalArgumentException("computed must be in [0,1] but is " + computed
                    + "! line: " + line);
        }
    }

    public double getComputed() {
        return computed;
    }

    public FeatureAggregate getFeatures() {
        return features;
    }

    public String getMessageGlobalId() {
        return messageGlobalId;
    }

    /**
     * 
     * @return as long in ms
     */
    public String getPublicationDate() {
        return publicationDate;
    }

    /**
     * 
     * @return as long in ms
     */
    public Date getPublicationDateAsDate() {
        return this.publicationDate == null ? null : new Date(Long.parseLong(this.publicationDate));
    }

    public double getTarget() {
        return target;
    }

    public int getTimeBin() {
        return timeBin;
    }

    public String getTimeBinUserKey() {
        return timeBin + "#" + userGlobalId;
    }

    public String getUserGlobalId() {
        return userGlobalId;
    }

    public boolean isBasedOnAdaptedTerms() {
        return basedOnAdaptedTerms;
    }

    public boolean isUnavailable() {
        return unavailable;
    }

    public void setBasedOnAdaptedTerms(boolean basedOnAdaptedTerms) {
        this.basedOnAdaptedTerms = basedOnAdaptedTerms;
    }

    public void setComputed(double computed) {
        this.computed = computed;
    }

    public void setFeatures(FeatureAggregate features) {
        this.features = features;
    }

    public void setMessageGlobalId(String messageGlobalId) {
        this.messageGlobalId = messageGlobalId;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate.getTime() + "";
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public void setTarget(double target) {
        this.target = target;
    }

    public void setTimeBin(int timeBin) {
        if (timeBin < 0) {
            throw new IllegalArgumentException("The timeBin cannot be less than zero! timeBin="
                    + timeBin);
        }
        this.timeBin = timeBin;
    }

    public void setUnavailable(boolean unavailable) {
        this.unavailable = unavailable;
    }

    public void setUserGlobalId(String userGlobalId) {
        this.userGlobalId = userGlobalId;
    }

    public String toParseableString() {

        String line = unavailable ? "-" : computed + "";
        line += " " + target;
        line += " " + messageGlobalId;
        line += " " + timeBin;
        line += " " + publicationDate;
        line += " " + userGlobalId;
        line += " " + basedOnAdaptedTerms;
        if (features == null) {
            line += " ";
        } else {
            line += " " + features.toParseableString();
        }
        return line;
    }

    @Override
    public String toString() {
        return "EvaluatorDataPoint [computed=" + computed + ", target=" + target + ", unavailable="
                + unavailable + ", messageId=" + messageGlobalId + ", timeBin=" + timeBin
                + ", userId="
                + userGlobalId + ", publicationDate=" + publicationDate + ", features=" + features
                + ", basedOnAdaptedTerms=" + basedOnAdaptedTerms + "]";
    }
}