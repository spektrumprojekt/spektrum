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
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.UserModelHolder;

public class UserToTermCollaborativeScoreComputer extends CollaborativeScoreComputer {

    private final TermVectorSimilarityComputer termVectorSimilarityComputer;

    public UserToTermCollaborativeScoreComputer(
            Persistence persistence,
            ObservationType[] observationTypesToUseForDataModel,
            TermVectorSimilarityComputer termVectorSimilarityComputer,
            boolean useGenericRecommender) {
        super(persistence, observationTypesToUseForDataModel, useGenericRecommender);
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

            GenericUserPreferenceArray genericUserPreferenceArray = new GenericUserPreferenceArray(
                    userModelHolder.getUserModelEntries().size());
            genericUserPreferenceArray.setUserID(0, user.getId());

            int index = 0;
            for (UserModelEntry umEntry : userModelHolder.getUserModelEntries().values()) {

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

        if (isOutPreferencesToFile()) {
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

        for (Term t : messageTerms) {
            float tPref;
            try {
                tPref = getRecommender().estimatePreference(userModel.getId(), t.getId());

                ScoredTerm sT = new ScoredTerm(t, tPref);
                UserModelEntry dummy = new UserModelEntry(userModel, sT);
                entries.put(t, dummy);
            } catch (TasteException e) {
                // ignore
            }

        }

        float estimate = this.termVectorSimilarityComputer.getSimilarity(
                message.getMessageGroup().getGlobalId(),
                entries,
                messageTerms);

        return estimate;
    }

    @Override
    public String getConfigurationDescription() {
        return super.getConfigurationDescription();
    }

}