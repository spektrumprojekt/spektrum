package de.spektrumprojekt.i.similarity.messagegroup;

import java.util.Comparator;

public class MessageGroupSimilarityComparator implements Comparator<MessageGroupSimilarity> {

    public final static MessageGroupSimilarityComparator INSTANCE = new MessageGroupSimilarityComparator();

    @Override
    public int compare(MessageGroupSimilarity o1, MessageGroupSimilarity o2) {

        if (o1.getSim() > o2.getSim()) {
            return 1;
        }
        if (o1.getSim() < o2.getSim()) {
            return -1;
        }
        return 0;
    }
}