package de.spektrumprojekt.i.timebased;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.commons.chain.CommandException;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.observation.Interest;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.learner.LearnerMessageContext;
import de.spektrumprojekt.i.learner.time.TimeBinnedUserModelEntryIntegrationStrategy;
import de.spektrumprojekt.i.ranker.RankerConfiguration;
import de.spektrumprojekt.persistence.Persistence;

public class TermCounterCommand implements Command<LearnerMessageContext> {

    public static final User USER = new User("TERM_COUNTING_USER");

    public static final String TERM_COUNT_USER_MODEL_NAME = "TERM_COUNT_USER_MODEL";

    private final Persistence persistence;

    private final TimeBinnedUserModelEntryIntegrationStrategy modelEntryIntegrationStrategy;

    public TermCounterCommand(RankerConfiguration configuration, Persistence persistence) {
        this.persistence = persistence;

        if (configuration.getShortTermMemoryConfiguration() == null
                || configuration.getShortTermMemoryConfiguration()
                        .getEnergyCalculationConfiguration() == null) {
            throw new IllegalArgumentException(
                    "configuration.getShortTermMemoryConfiguration and "
                            + "configuration.getShortTermMemoryConfiguration().getEnergyCalculationConfiguration() "
                            + "cannot be null");
        }
        modelEntryIntegrationStrategy = new TimeBinnedUserModelEntryIntegrationStrategy(0,
                DateUtils.MILLIS_PER_DAY * 31 * 10, configuration
                        .getShortTermMemoryConfiguration().getPrecision());

    }

    @Override
    public String getConfigurationDescription() {
        return "TermCounterCommand" + " modelEntryIntegrationStrategy="
                + modelEntryIntegrationStrategy.getConfigurationDescription();
    }

    @Override
    public void process(LearnerMessageContext context) throws CommandException {

        Collection<Term> terms = MessageHelper.getAllTerms(context.getMessage());
        UserModel userModel = persistence.getOrCreateUserModelByUser(USER.getGlobalId(),
                TERM_COUNT_USER_MODEL_NAME);
        Map<Term, UserModelEntry> userModelEntries = persistence.getUserModelEntriesForTerms(
                userModel, terms);
        List<UserModelEntry> changedEntries = new LinkedList<UserModelEntry>();
        for (Term term : terms) {
            UserModelEntry entry = userModelEntries.get(term);
            if (entry == null) {
                entry = new UserModelEntry(userModel, new ScoredTerm(term, 1));
            }
            modelEntryIntegrationStrategy.integrate(entry, Interest.NORMAL, new ScoredTerm(
                    term, 1), context.getMessage().getPublicationDate());
            changedEntries.add(entry);
        }
        persistence.storeOrUpdateUserModelEntries(userModel, changedEntries);

    }
}
