package uk.gov.justice.services.core.extension;

import uk.gov.justice.services.core.annotation.Event;

/**
 * Event representing the occurrence of a class with an {@link Event} annotation having been
 * identified by the framework.
 */
public class EventFoundEvent {

    private final Class<?> clazz;
    private final String eventName;

    public EventFoundEvent(final Class<?> clazz, final String eventName) {
        this.clazz = clazz;
        this.eventName = eventName;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getEventName() {
        return eventName;
    }
}
