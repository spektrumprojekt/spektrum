package de.spektrumprojekt.commons.event;

public interface EventListener<T extends Event> {

    public void onEvent(T event);
}
