package de.spektrumprojekt.datamodel.user;

import java.util.Comparator;

public class UserModelEntryTimeBinComparator implements
        Comparator<UserModelEntryTimeBin> {

    public static final UserModelEntryTimeBinComparator INSTANCE = new UserModelEntryTimeBinComparator();

    @Override
    public int compare(UserModelEntryTimeBin o1, UserModelEntryTimeBin o2) {
        long diff = o1.getTimeBinStart() - o2.getTimeBinStart();

        if (diff < 0) {
            return -1;
        }
        if (diff > 0) {
            return 1;
        }
        return 0;
    }
}