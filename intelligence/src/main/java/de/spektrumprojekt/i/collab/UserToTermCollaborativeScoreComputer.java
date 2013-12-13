package de.spektrumprojekt.i.collab;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.ranker.CollaborativeConfiguration;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.UserModelHolder;

public class UserToTermCollaborativeScoreComputer extends CollaborativeScoreComputer {

    private final TermVectorSimilarityComputer termVectorSimilarityComputer;

    public UserToTermCollaborativeScoreComputer(
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
        UserModelHolder userModelHolder = getPersistence().getUserModelHolder(
                UserModel.DEFAULT_USER_MODEL_TYPE, user);
        if (userModelHolder != null && userModelHolder.getUserModelEntries().size() > 0) {

            Map<Term, UserModelEntry> entries = filterEntriesForCreatingUserPreferences(user,
                    userModelHolder);

            GenericUserPreferenceArray genericUserPreferenceArray = new GenericUserPreferenceArray(
                    entries.size());
            genericUserPreferenceArray.setUserID(0, user.getId());

            int index = 0;
            for (UserModelEntry umEntry : entries.values()) {

                createUserTermPreferences(genericUserPreferenceArray, user,
                        umEntry.getScoredTerm(), fw,
                        index++);
            }

            userData.put(user.getId(), genericUserPreferenceArray);
        }

    }

    protected void createUserTermPreferences(
            GenericUserPreferenceArray genericUserPreferenceArray,
            User user,
            ScoredTerm scoredTerm,
            FileWriter fw,
            int index)
            throws IOException {

        float pref = convertScoreToMahoutValue(scoredTerm.getWeight());

        if (pref != 0) {
            incrementPreferenceCount();
        }
        if (pref == 1) {
            incrementPositivePreferenceCount();
        }
        if (pref == -1) {
            incrementNegativePreferenceCount();
        }
        if (pref < -1 || pref > 1) {
            throw new RuntimeException(scoredTerm + " " + user.getGlobalId() + " "
                    + pref);
        }

        genericUserPreferenceArray.setItemID(index, scoredTerm.getTerm().getId());
        genericUserPreferenceArray.setValue(index, pref);

        if (getCollaborativeConfiguration().isOutPreferencesToFile()) {
            fw.write(index + " " + scoredTerm.getTerm().getValue() + " " + user.getId() + " "
                    + pref + "\n");
        }
    }

    @Override
    protected float estimate(User user, Message message) throws TasteException {

        Collection<Term> messageTerms = MessageHelper.getAllTerms(message);

        Map<Term, UserModelEntry> entries = new HashMap<Term, UserModelEntry>();
        UserModel userModel = getPersistence().getOrCreateUserModelByUser(user.getGlobalId(),
                UserModel.DEFAULT_USER_MODEL_TYPE);

        boolean allMustExist = false;
        if (message.getAuthorGlobalId().equals(user.getGlobalId())) {
            allMustExist = true;
        }

        for (Term t : messageTerms) {
            float tPref;
            try {
                tPref = getRecommender().estimatePreference(userModel.getUser().getId(), t.getId());

                if (!Float.isNaN(tPref)) {
                    float score = convertScoreFromMahoutValue(tPref, true);
                    ScoredTerm sT = new ScoredTerm(t, score);
                    UserModelEntry dummy = new UserModelEntry(userModel, sT);
                    entries.put(t, dummy);
                }
                if (allMustExist && (tPref == -1 || Float.isNaN(tPref))) {
                    throw new IllegalStateException("term " + t
                            + " should exist as preference for user: " + user + " message: "
                            + message);
                }
            } catch (TasteException e) {
                // ignore
            }

        }

        String messageGroupGlobalId = message.getMessageGroup() == null ? null : message
                .getMessageGroup().getGlobalId();
        float estimate = this.termVectorSimilarityComputer.getSimilarity(
                messageGroupGlobalId,
                entries,
                messageTerms);

        return convertScoreToMahoutValue(estimate);
    }

    protected Map<Term, UserModelEntry> filterEntriesForCreatingUserPreferences(User user,
            UserModelHolder userModelHolder) {
        return userModelHolder.getUserModelEntries();
    }

    @Override
    public String getConfigurationDescription() {
        return super.getConfigurationDescription();
    }

}