package de.spektrumprojekt.i.learner.chain;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.i.learner.LearnerMessageContext;
import de.spektrumprojekt.persistence.Persistence;

public class StoreObservationCommand implements Command<LearnerMessageContext> {

    private final Persistence persistence;

    public StoreObservationCommand(Persistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void process(LearnerMessageContext context) {

        this.persistence.storeObservation(context.getObservation());
    }
}
