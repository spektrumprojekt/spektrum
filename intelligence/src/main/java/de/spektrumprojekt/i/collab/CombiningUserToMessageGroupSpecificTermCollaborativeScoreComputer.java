package de.spektrumprojekt.i.collab;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.ranker.CollaborativeConfiguration;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.persistence.Persistence;

/**
 * Combines several {@link UserToMessageGroupSpecificTermCollaborativeScoreComputer} for each
 * message group.
 * 
 * 
 */
public class CombiningUserToMessageGroupSpecificTermCollaborativeScoreComputer extends
        CollaborativeScoreComputer {

    private TermVectorSimilarityComputer termVectorSimilarityComputer;

    public CombiningUserToMessageGroupSpecificTermCollaborativeScoreComputer(
            Persistence persistence,
            CollaborativeConfiguration collaborativeConfiguration,
            TermVectorSimilarityComputer termVectorSimilarityComputer) {
        super(persistence, collaborativeConfiguration);

        if (termVectorSimilarityComputer == null) {
            throw new IllegalArgumentException("termVectorSimilarityComputer cannot be null.");
        }
        this.termVectorSimilarityComputer = termVectorSimilarityComputer;
    }

    @Override
    protected void createUserPreference(FastByIDMap<PreferenceArray> userData, User user,
            FileWriter fw) throws IOException {
        throw new IllegalStateException("should not be called.");
    }

    @Override
    protected float estimate(User user, Message message) throws TasteException {
        throw new IllegalStateException("should not be called.");
    }

    @Override
    public void init() throws InconsistentDataException, TasteException {
        // nothing to do
    }

    @Override
    public void run(Collection<Message> messagesToRun) throws TasteException {

        this.setMessageScores(new HashSet<UserMessageScore>());

        Map<MessageGroup, Collection<Message>> mg2messages = MessageHelper
                .splitByMessageGroup(messagesToRun);

        for (Entry<MessageGroup, Collection<Message>> mg2messageEntry : mg2messages.entrySet()) {

            UserToMessageGroupSpecificTermCollaborativeScoreComputer mgCollabComputer = new UserToMessageGroupSpecificTermCollaborativeScoreComputer(
                    getPersistence(),
                    getCollaborativeConfiguration(),
                    termVectorSimilarityComputer,
                    mg2messageEntry.getKey());

            mgCollabComputer.init();
            mgCollabComputer.run(mg2messageEntry.getValue());

            this.setNanScores(this.getNanScores() + mgCollabComputer.getNanScores());
            this.setNegativePreferenceCount(this.getNegativePreferenceCount()
                    + mgCollabComputer.getNegativePreferenceCount());
            this.setNonZeroRanks(this.getNonZeroRanks() + mgCollabComputer.getNonZeroRanks());
            this.setPositivePreferenceCount(this.getPositivePreferenceCount()
                    + mgCollabComputer.getPositivePreferenceCount());
            this.setPreferenceCount(this.getPreferenceCount()
                    + mgCollabComputer.getPreferenceCount());

            this.getMessageScores().addAll(mgCollabComputer.getMessageScores());
        }
    }

}