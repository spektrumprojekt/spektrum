package de.spektrumprojekt.i.collab;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.scorer.CollaborativeConfiguration;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.UserModelHolder;

public class UserToMessageGroupSpecificTermCollaborativeScoreComputer extends
        UserToTermCollaborativeScoreComputer {

    private final MessageGroup messageGroup;

    public UserToMessageGroupSpecificTermCollaborativeScoreComputer(
            Persistence persistence,
            CollaborativeConfiguration collaborativeConfiguration,
            TermVectorSimilarityComputer termVectorSimilarityComputer,
            MessageGroup messageGroup) {
        super(persistence, collaborativeConfiguration, termVectorSimilarityComputer);
        if (messageGroup == null) {
            throw new IllegalArgumentException("messageGroup cannot be null.");
        }
        this.messageGroup = messageGroup;
    }

    @Override
    protected Map<Term, UserModelEntry> filterEntriesForCreatingUserPreferences(User user,
            UserModelHolder userModelHolder) {
        Map<Term, UserModelEntry> filtered = new HashMap<Term, UserModelEntry>();
        Map<Term, UserModelEntry> allEntries = super.filterEntriesForCreatingUserPreferences(
                user, userModelHolder);
        if (allEntries != null) {
            for (Entry<Term, UserModelEntry> entry : allEntries.entrySet()) {
                if (entry.getKey().getMessageGroupId().equals(this.messageGroup.getId())) {
                    filtered.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return filtered;
    }

    @Override
    public String getConfigurationDescription() {
        return super.getConfigurationDescription() + " messageGroup: " + messageGroup;
    }

}