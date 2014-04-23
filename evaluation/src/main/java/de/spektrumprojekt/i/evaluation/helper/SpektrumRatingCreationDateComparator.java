package de.spektrumprojekt.i.evaluation.helper;

import java.util.Comparator;


public class SpektrumRatingCreationDateComparator implements
        Comparator<SpektrumRating> {
    public int compare(SpektrumRating o1, SpektrumRating o2) {
        long diff = o1.getMessage().getPublicationDate().getTime()
                - o1.getMessage().getPublicationDate().getTime();
        if (diff == 0) {
            diff = o1.getMessage().getId() - o2.getMessage().getId();
        }
        if (diff > 0) {
            return 1;
        }
        if (diff < 0) {
            return -1;
        }
        return 0;
    }
}