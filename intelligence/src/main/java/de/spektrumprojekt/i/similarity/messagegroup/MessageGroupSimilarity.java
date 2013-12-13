package de.spektrumprojekt.i.similarity.messagegroup;

import org.apache.commons.lang.StringUtils;

import de.spektrumprojekt.commons.output.SpektrumParseableElement;

public class MessageGroupSimilarity implements SpektrumParseableElement {
    public static String getColumnHeader() {
        return StringUtils.join(new String[] {
                "messageGroupId1",
                "messageGroupId2",
                "messageGroupGlobalId1",
                "messageGroupGlobalId2",
                "sim",
                "computationDate",
                "termsMG1Count",
                "termsMG2Count",
                "unionTermsCount",
                "intersectedTermCount"
        }, " ");
    }

    private Long messageGroupId1;
    private Long messageGroupId2;
    private String messageGroupGlobalId1 = "";
    private String messageGroupGlobalId2 = "";

    private int termsMG1Count;

    private int termsMG2Count;

    private int unionTermsCount;

    private int intersectedTermCount;

    private float sim;
    private long computationDate;

    public MessageGroupSimilarity(Long messageGroupId1, Long messageGroupId2) {
        if (messageGroupId1 == null) {
            throw new IllegalArgumentException("messageGroupId1 cannot be null");
        }
        if (messageGroupId2 == null) {
            throw new IllegalArgumentException("messageGroupId2 cannot be null");
        }
        this.messageGroupId1 = messageGroupId1;
        this.messageGroupId2 = messageGroupId2;
    }

    public MessageGroupSimilarity(String line) {
        String[] vals = line.split(" ");
        int index = 0;
        messageGroupId1 = new Long(vals[index++]);
        messageGroupId2 = new Long(vals[index++]);

        messageGroupGlobalId1 = vals[index++];
        messageGroupGlobalId2 = vals[index++];

        sim = Float.parseFloat(vals[index++]);
        computationDate = Long.parseLong(vals[index++]);

        termsMG1Count = Integer.parseInt(vals[index++]);
        termsMG2Count = Integer.parseInt(vals[index++]);
        unionTermsCount = Integer.parseInt(vals[index++]);
        intersectedTermCount = Integer.parseInt(vals[index++]);

    }

    public long getComputationDate() {
        return computationDate;
    }

    public int getIntersectedTermCount() {
        return intersectedTermCount;
    }

    public String getMessageGroupGlobalId1() {
        return messageGroupGlobalId1;
    }

    public String getMessageGroupGlobalId2() {
        return messageGroupGlobalId2;
    }

    public Long getMessageGroupId1() {
        return messageGroupId1;
    }

    public Long getMessageGroupId2() {
        return messageGroupId2;
    }

    public float getSim() {
        return sim;
    }

    public int getTermsMG1Count() {
        return termsMG1Count;
    }

    public int getTermsMG2Count() {
        return termsMG2Count;
    }

    public int getUnionTermsCount() {
        return unionTermsCount;
    }

    /**
     * 
     * @param messageGroupId
     * @param ignoreIdentity
     *            if true similarities with both having the same id will be ignored
     * @return true if one message group matches the given one
     */
    public boolean matchesMessageGroupId(Long messageGroupId, boolean ignoreIdentity) {
        if (ignoreIdentity && this.messageGroupId1.equals(this.messageGroupId2)) {
            return false;
        }
        if (this.messageGroupId1.equals(messageGroupId)
                || this.messageGroupId2.equals(messageGroupId)) {
            return true;
        }
        return false;
    }

    public void setComputationDate(long computationDate) {
        this.computationDate = computationDate;
    }

    public void setIntersectedTermCount(int intersectedTermCount) {
        this.intersectedTermCount = intersectedTermCount;
    }

    public void setMessageGroupGlobalId1(String messageGroupGlobalId1) {
        this.messageGroupGlobalId1 = messageGroupGlobalId1;
    }

    public void setMessageGroupGlobalId2(String messageGroupGlobalId2) {
        this.messageGroupGlobalId2 = messageGroupGlobalId2;
    }

    public void setMessageGroupId1(Long messageGroupId1) {
        this.messageGroupId1 = messageGroupId1;
    }

    public void setMessageGroupId2(Long messageGroupId2) {
        this.messageGroupId2 = messageGroupId2;
    }

    public void setSim(float sim) {
        this.sim = sim;
    }

    public void setTermsMG1Count(int termsMG1Count) {
        this.termsMG1Count = termsMG1Count;
    }

    public void setTermsMG2Count(int termsMG2Count) {
        this.termsMG2Count = termsMG2Count;
    }

    public void setUnionTermsCount(int unionTermsCount) {
        this.unionTermsCount = unionTermsCount;
    }

    @Override
    public String toParseableString() {
        return StringUtils.join(new String[] {
                this.messageGroupId1.toString(),
                this.messageGroupId2.toString(),
                this.messageGroupGlobalId1,
                this.messageGroupGlobalId2,
                String.valueOf(this.sim),
                String.valueOf(this.computationDate),
                String.valueOf(termsMG1Count),
                String.valueOf(termsMG2Count),
                String.valueOf(unionTermsCount),
                String.valueOf(intersectedTermCount),

        }, " ");
    }

}