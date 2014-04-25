package de.spektrumprojekt.datamodel.test;

import java.util.Arrays;

import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.persistence.Persistence;

/**
 * Helper for tests to create some entries.
 */
public final class UserModelTestHelper {

    public static void createSomeUserModelEntries(
            Persistence persistence,
            MessageGroup mg,
            UserModel userModel1,
            UserModel userModel2,
            UserModel userModel3) {

        Term[] terms = getSomeTerms(persistence, mg);

        Term term1 = terms[0];
        Term term2 = terms[1];
        Term term3 = terms[2];

        // create user model
        if (userModel1 != null) {
            UserModelEntry ume1_1 = new UserModelEntry(userModel1, new ScoredTerm(term1, 1f));
            UserModelEntry ume1_2 = new UserModelEntry(userModel1, new ScoredTerm(term2, 1f));
            UserModelEntry ume1_3 = new UserModelEntry(userModel1, new ScoredTerm(term3, 1f));

            persistence.storeOrUpdateUserModelEntries(userModel1,
                    Arrays.asList(ume1_1, ume1_2, ume1_3));
        }

        if (userModel2 != null) {
            UserModelEntry ume2_1 = new UserModelEntry(userModel2, new ScoredTerm(term1, 1f));
            UserModelEntry ume2_2 = new UserModelEntry(userModel2, new ScoredTerm(term2, 1f));
            UserModelEntry ume2_3 = new UserModelEntry(userModel2, new ScoredTerm(term3, 1f));

            persistence.storeOrUpdateUserModelEntries(userModel2,
                    Arrays.asList(ume2_1, ume2_2, ume2_3));
        }

        if (userModel3 != null) {
            UserModelEntry ume3_1 = new UserModelEntry(userModel3, new ScoredTerm(term1, 0f));
            UserModelEntry ume3_2 = new UserModelEntry(userModel3, new ScoredTerm(term2, 1f));
            UserModelEntry ume3_3 = new UserModelEntry(userModel3, new ScoredTerm(term3, 1f));

            persistence.storeOrUpdateUserModelEntries(userModel3,
                    Arrays.asList(ume3_1, ume3_2, ume3_3));
        }
    }

    public static Term[] getSomeTerms(Persistence persistence, MessageGroup mg) {
        return new Term[] {
                persistence.getOrCreateTerm(TermCategory.TERM,
                        Term.getMessageGroupSpecificTermValue(mg, "term1")),
                persistence.getOrCreateTerm(TermCategory.TERM,
                        Term.getMessageGroupSpecificTermValue(mg, "term2")),
                persistence.getOrCreateTerm(TermCategory.TERM,
                        Term.getMessageGroupSpecificTermValue(mg, "term3")),
        };
    }

    private UserModelTestHelper() {
        // no op
    }
}
