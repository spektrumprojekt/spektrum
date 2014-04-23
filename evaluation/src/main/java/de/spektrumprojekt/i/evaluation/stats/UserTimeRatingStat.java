package de.spektrumprojekt.i.evaluation.stats;

import java.util.Date;

public class UserTimeRatingStat {

    public String userId;
    public long dateField;
    public Date date;

    public final RatingCount ratingCount = new RatingCount();
}