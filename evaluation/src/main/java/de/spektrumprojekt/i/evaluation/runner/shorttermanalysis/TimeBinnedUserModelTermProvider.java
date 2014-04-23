package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserModelEntryTimeBin;
import de.spektrumprojekt.persistence.Persistence;

/**
 * Provides terms of a time binned user model of a specific user
 * 
 * @author Torsten
 * 
 */
public class TimeBinnedUserModelTermProvider implements TermProvider {

    private final Persistence persistence;
    private final String userModelType;
    private final Date startDate;
    private final String userGlobalId;

    public TimeBinnedUserModelTermProvider(
            Persistence persistence,
            String userModelType,
            Date startDate,
            String userGlobalId) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (userModelType == null) {
            throw new IllegalArgumentException("userModelType cannot be null.");
        }
        if (userGlobalId == null) {
            throw new IllegalArgumentException("userGlobalId cannot be null.");
        }

        this.persistence = persistence;
        this.userModelType = userModelType;
        this.userGlobalId = userGlobalId;
        this.startDate = startDate;
    }

    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " startDate: " + startDate
                + " userModelType: " + userModelType
                + " userGlobalId: " + userGlobalId;
    }

    public String getFileAppendix() {
        return "_userModel_";
    }

    public Collection<Entry> getTerms() {
        Set<Entry> result = new HashSet<Entry>();
        UserModel userModel = persistence.getOrCreateUserModelByUser(
                userGlobalId, userModelType);
        Map<Term, UserModelEntry> userModelEntries = persistence
                .getUserModelEntriesForTerms(userModel,
                        persistence.getAllTerms());

        for (UserModelEntry userModelEntry : userModelEntries.values()) {
            for (UserModelEntryTimeBin timeBin : userModelEntry.getTimeBinEntries()) {

                if (startDate == null || timeBin.getTimeBinStart() >= startDate.getTime()) {
                    result.add(new Entry(userModelEntry.getScoredTerm().getTerm().getValue(),
                            new Date(timeBin.getTimeBinStart()), timeBin.getScoreSum()));
                }

            }
        }
        return result;
    }
}
