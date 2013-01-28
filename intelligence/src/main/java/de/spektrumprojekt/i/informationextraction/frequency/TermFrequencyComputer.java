package de.spektrumprojekt.i.informationextraction.frequency;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.persistence.Persistence;

public class TermFrequencyComputer implements ConfigurationDescriptable {

    private final Persistence persistence;

    public TermFrequencyComputer(Persistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    public void run() {

        Date fromDate = new Date(0);

        this.persistence.resetTermCount();

        Collection<Message> messages = this.persistence.getMessagesSince(fromDate);
        Collection<Term> termsChanged = new HashSet<Term>();

        long allTermCount = 0;
        long uniqueTermCount = 0;
        for (Message message : messages) {
            for (Term term : MessageHelper.getAllTerms(message)) {
                if (term.getCount() == 0) {
                    uniqueTermCount++;
                }
                uniqueTermCount++;
                term.setCount(term.getCount() + 1);
                termsChanged.add(term);
            }
        }

        this.persistence.updateTerms(termsChanged);

        // TODO where to store the overall term counts ?

    }
}
