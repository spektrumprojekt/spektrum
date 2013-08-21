package de.spektrumprojekt.i.timebased;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.commons.chain.CommandException;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.informationextraction.InformationExtractionConfiguration;
import de.spektrumprojekt.i.learner.LearnerMessageContext;
import de.spektrumprojekt.persistence.Persistence;

public class TermCounterCommand implements Command<LearnerMessageContext> {

    public static final User USER = new User("TERM_COUNTING_USER");

    public static final String TERM_COUNT_USER_MODEL_NAME = "TERM_COUNT_USER_MODEL";

    Persistence persistence;

    InformationExtractionConfiguration informationExtractionConfiguration;

    public TermCounterCommand(Persistence persistence,
            InformationExtractionConfiguration informationExtractionConfiguration) {
        super();
        this.persistence = persistence;
        this.informationExtractionConfiguration = informationExtractionConfiguration;
    }

    @Override
    public String getConfigurationDescription() {
        return "TermCounterCommand";
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
            entry.setScoreCount(entry.getScoreCount() + 1);
            changedEntries.add(entry);
        }
        persistence.storeOrUpdateUserModelEntries(userModel, changedEntries);

    }
}
