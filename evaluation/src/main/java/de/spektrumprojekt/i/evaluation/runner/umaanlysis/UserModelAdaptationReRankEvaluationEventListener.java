package de.spektrumprojekt.i.evaluation.runner.umaanlysis;

import java.util.HashMap;
import java.util.Map;

import de.spektrumprojekt.commons.event.EventListener;
import de.spektrumprojekt.i.learner.adaptation.UserModelAdaptationReScoreEvent;

public class UserModelAdaptationReRankEvaluationEventListener implements
        EventListener<UserModelAdaptationReScoreEvent> {

    private final Map<String, UserModelAdaptationReScoreEvent> events = new HashMap<String, UserModelAdaptationReScoreEvent>();

    public void clearEvents() {
        this.events.clear();
    }

    public Map<String, UserModelAdaptationReScoreEvent> getAndRemoveEvents() {
        Map<String, UserModelAdaptationReScoreEvent> events = new HashMap<String, UserModelAdaptationReScoreEvent>(
                this.events);
        this.events.clear();

        return events;

    }

    public UserModelAdaptationReScoreEvent getEventForUser(String userGlobalId) {
        return this.events.get(userGlobalId);
    }

    public void onEvent(UserModelAdaptationReScoreEvent event) {
        if (this.events.get(event.getAdaptationMessage().getUserGlobalId()) != null) {
            throw new IllegalStateException("the event should be removed!");
        }
        this.events.put(event.getAdaptationMessage().getUserGlobalId(), event);
    }
}