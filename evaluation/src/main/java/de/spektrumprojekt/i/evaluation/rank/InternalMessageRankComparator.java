package de.spektrumprojekt.i.evaluation.rank;

import java.util.Comparator;
import java.util.Date;

import de.spektrumprojekt.i.evaluation.measure.top.TopRankMessageComputer;

/**
 * Comparator that orders ranks by their same date field ascending and the ranks descending
 * 
 * @author Torsten
 * 
 */
public class InternalMessageRankComparator implements Comparator<InternalMessageRank> {

    private final int field;

    private final boolean useIdForEqualCompares;

    /**
     * 
     * @param field
     *            the field of the calendar to use for comparison
     * @param useIdForEqualCompares
     *            if true: if the compare of the date and score is equal it will use the message id
     *            for comparison to avoid a random sort.
     */
    public InternalMessageRankComparator(int field, boolean useIdForEqualCompares) {
        this.field = field;
        this.useIdForEqualCompares = useIdForEqualCompares;
    }

    public int compare(InternalMessageRank o1, InternalMessageRank o2) {

        Date cal1 = o1.getPublicationDate();
        Date cal2 = o2.getPublicationDate();

        int diff = TopRankMessageComputer.compare(cal1, cal2, field);
        if (diff != 0) {
            return -diff;
        }
        double diff3 = o1.getScore() - o2.getScore();

        if (diff3 > 0) {
            return -1;
        }
        if (diff3 < 0) {
            return 1;
        }
        if (useIdForEqualCompares) {
            return o1.getMessageId().compareTo(o2.getMessageId());
        }
        return 0;

    }

}