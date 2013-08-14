package de.spektrumprojekt.i.user.hits;

import java.util.Collection;
import java.util.List;

import de.spektrumprojekt.i.user.UserScore;
import de.spektrumprojekt.i.user.UserToUserInterestSelector;

public class HITSUserSelector implements UserToUserInterestSelector {

    private final HITSUserMentionComputer hitsUserMentionComputer;

    public HITSUserSelector(HITSUserMentionComputer hitsUserMentionComputer) {
        if (hitsUserMentionComputer == null) {
            throw new IllegalArgumentException("hitsUserMentionComputer cannot be null.");
        }
        this.hitsUserMentionComputer = hitsUserMentionComputer;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " hitsUserMentionComputer: "
                + hitsUserMentionComputer.getConfigurationDescription();
    }

    @Override
    public List<UserScore> getUserToUserInterest(String userGlobalId, String messageGroupGlobalId,
            Collection<String> userGlobalIdsToConsider) {

        return hitsUserMentionComputer.getUserToUserInterest(messageGroupGlobalId,
                userGlobalIdsToConsider);
    }

}
