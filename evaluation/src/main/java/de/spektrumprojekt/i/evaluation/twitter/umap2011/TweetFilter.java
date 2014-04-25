package de.spektrumprojekt.i.evaluation.twitter.umap2011;

import java.util.Arrays;
import java.util.Date;

public class TweetFilter {

    public Date minDate;
    public Date maxDate;

    public String[] usernames;

    @Override
    public String toString() {
        return "TweetFilter [minDate=" + minDate + ", maxDate=" + maxDate + ", usernames="
                + Arrays.toString(usernames) + "]";
    }

}