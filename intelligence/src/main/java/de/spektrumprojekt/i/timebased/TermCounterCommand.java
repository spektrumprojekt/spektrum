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

    Persistence persistence;
    RankerConfiguration configuration;
    TimeBinnedUserModelEntryIntegrationStrategy modelEntryIntegrationStrategy;

    private boolean disabled;

    public TermCounterCommand(RankerConfiguration configuration, Persistence persistence) {
        super();
        this.persistence = persistence;
        this.configuration = configuration;
        if (configuration.getShortTermMemoryConfiguration() == null
                || configuration.getShortTermMemoryConfiguration()
                        .getEnergyCalculationConfiguration() == null) {
            disabled = true;
        } else {
            modelEntryIntegrationStrategy = new TimeBinnedUserModelEntryIntegrationStrategy(0,
                    DateUtils.MILLIS_PER_DAY * 31 * 10, configuration
                            .getShortTermMemoryConfiguration().getPrecision());
        }
    }

    @Override
    public String getConfigurationDescription() {
        return "TermCounterCommand";
    }

    @Override
    public void process(LearnerMessageContext context) throws CommandException {
        if (!disabled) {
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
}
