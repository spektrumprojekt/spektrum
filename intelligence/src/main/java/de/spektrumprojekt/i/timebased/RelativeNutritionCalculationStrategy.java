package de.spektrumprojekt.i.timebased;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserModelEntryTimeBin;
import de.spektrumprojekt.persistence.Persistence;

public class RelativeNutritionCalculationStrategy implements NutritionCalculationStrategy {

    private Persistence persistence;

    @Override
    public float[] getNutrition(UserModelEntry entry) {
        if (persistence == null) {
            throw new RuntimeException("Initialize persistence before using this strategy!");
        }
        Term term = entry.getScoredTerm().getTerm();
        List<Term> terms = new LinkedList<Term>();
        terms.add(term);
        UserModel userModel = persistence.getOrCreateUserModelByUser(
                TermCounterCommand.USER.getGlobalId(),
                TermCounterCommand.TERM_COUNT_USER_MODEL_NAME);
        Map<Term, UserModelEntry> globalUserModelEntries = persistence.getUserModelEntriesForTerms(
                userModel, terms);
        UserModelEntry globalUserModelEntry = globalUserModelEntries.get(term);
        Collection<UserModelEntryTimeBin> userTimeBinEntries = entry.getTimeBinEntries();
        float[] result = new float[userTimeBinEntries.size()];
        int i = 0;
        for (UserModelEntryTimeBin userbin : userTimeBinEntries) {
            UserModelEntryTimeBin globalBin = globalUserModelEntry
                    .getUserModelEntryTimeBinByStartTime(userbin.getTimeBinStart());
            result[i] = userbin.getScoreSum() / globalBin.getScoreCount() * userbin.getScoreCount();
            i++;
        }
        return result;
    }

    public Persistence getPersistence() {
        return persistence;
    }

    public void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }
}
