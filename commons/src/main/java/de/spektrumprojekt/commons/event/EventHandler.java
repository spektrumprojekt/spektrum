package de.spektrumprojekt.commons.event;

import java.util.ArrayList;
import java.util.List;

public class EventHandler<E extends Event> {

    private final List<EventListener<E>> eventListeners = new ArrayList<EventListener<E>>();

    public void addEventListener(EventListener<E> eventListener) {
        if (eventListener == null) {
            throw new IllegalArgumentException("eventListener cannot be null!");
        }
        this.eventListeners.add(eventListener);
    }

    public void fire(E event) {
        for (EventListener<E> eventListener : this.eventListeners) {
            eventListener.onEvent(event);
        }
    }

    public int getEventListenersSize() {
        return this.eventListeners.size();
    }

    public boolean removeEventListener(EventListener<E> eventListener) {
        return this.eventListeners.remove(eventListener);
    }
}
