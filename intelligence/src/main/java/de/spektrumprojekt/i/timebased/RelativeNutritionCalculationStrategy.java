package de.spektrumprojekt.i.timebased;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserModelEntryTimeBin;
import de.spektrumprojekt.persistence.Persistence;

public class RelativeNutritionCalculationStrategy implements NutritionCalculationStrategy {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RelativeNutritionCalculationStrategy.class);

    private final BinAggregatedUserModelEntryDecorator binAggregatedUserModelEntryDecorator;

    public RelativeNutritionCalculationStrategy(
            BinAggregatedUserModelEntryDecorator binAggregatedUserModelEntryDecorator) {
        this.binAggregatedUserModelEntryDecorator = binAggregatedUserModelEntryDecorator;
    }

    @Override
    public float[] getNutrition(UserModelEntry entry, Persistence persistence) {
        if (persistence == null) {
            throw new RuntimeException("Persistence must not be null!");
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
        binAggregatedUserModelEntryDecorator.setEntry(globalUserModelEntry);
        globalUserModelEntry = binAggregatedUserModelEntryDecorator;
        Collection<UserModelEntryTimeBin> userTimeBinEntries = entry.getTimeBinEntries();
        float[] result = new float[userTimeBinEntries.size()];
        int i = 0;
        for (UserModelEntryTimeBin userbin : userTimeBinEntries) {
            UserModelEntryTimeBin globalBin = globalUserModelEntry
                    .getUserModelEntryTimeBinByStartTime(userbin.getTimeBinStart());
            if (globalBin == null) {
                LOGGER.warn("globalBin was null!");
                result[i] = 0;
            } else {
                result[i] = userbin.getScoreSum() / globalBin.getScoreCount()
                        * userbin.getScoreCount();
            }
            i++;
        }
        return result;
    }
}
