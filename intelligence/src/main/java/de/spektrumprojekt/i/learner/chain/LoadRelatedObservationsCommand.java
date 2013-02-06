package de.spektrumprojekt.i.learner.chain;

import java.util.Collection;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.i.learner.LearnerMessageContext;
import de.spektrumprojekt.persistence.Persistence;

public class LoadRelatedObservationsCommand implements Command<LearnerMessageContext> {

    private final Persistence persistence;

    public LoadRelatedObservationsCommand(Persistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void process(LearnerMessageContext context) {

        Observation observation = context.getObservation();
        Collection<Observation> observations = this.persistence.getObservations(
                observation.getUserGlobalId(), observation.getMessageGlobalId(),
                observation.getObservationType());

        if (observations != null) {
            context.getRelatedObservations().addAll(observations);
        }

    }
}
