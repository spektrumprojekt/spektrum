package de.spektrumprojekt.i.similarity.user;

import java.util.Comparator;

/**
 * Sorts {@link UserScore}'s, starting with the highest (most similar).
 * 
 */
public class UserScoreComparator implements Comparator<UserScore> {

    public final static UserScoreComparator INSTANCE = new UserScoreComparator();

    @Override
    public int compare(UserScore o1, UserScore o2) {
        if (o1.getScore() < o2.getScore()) {
            return 1;
        }
        if (o1.getScore() > o2.getScore()) {
            return -1;
        }
        return 0;
    }
}