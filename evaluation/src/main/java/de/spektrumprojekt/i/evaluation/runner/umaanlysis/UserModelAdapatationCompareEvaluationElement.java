package de.spektrumprojekt.i.evaluation.runner.umaanlysis;

import org.apache.commons.lang.StringUtils;

import de.spektrumprojekt.commons.output.SpektrumParseableElement;

public class UserModelAdapatationCompareEvaluationElement implements SpektrumParseableElement {

    public static String getColumnHeaders() {
        return StringUtils.join(new String[] {
                "computedRankBefore",
                "computedAdaptedRank",
                "targetRank",
                "messageId",
                "userId"
        }, " ");
    }

    public Double computedRankBefore;
    public Double computedAdaptedRank;
    public Double targetRank;
    public String messageId;
    public String userId;

    public UserModelAdapatationCompareEvaluationElement() {
    }

    public UserModelAdapatationCompareEvaluationElement(String line) {
        String[] parsed = line.split(" ");
        int index = 0;
        computedRankBefore = "-".equals(parsed[index]) ? null : Double
                .parseDouble(parsed[index]);
        index++;
        computedAdaptedRank = "-".equals(parsed[index]) ? null : Double
                .parseDouble(parsed[index]);
        index++;
        targetRank = "-".equals(parsed[index]) ? null : Double.parseDouble(parsed[index]);
        index++;
        messageId = parsed[index++];
        userId = parsed[index++];
    }

    public String toParseableString() {

        return StringUtils.join(new String[] {
                computedRankBefore == null ? "-" : "" + computedRankBefore,
                computedAdaptedRank == null ? "-" : "" + computedAdaptedRank,
                targetRank == null ? "-" : "" + targetRank,
                messageId,
                userId

        }, " ");
    }

}