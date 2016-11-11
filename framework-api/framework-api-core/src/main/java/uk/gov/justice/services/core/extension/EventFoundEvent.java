package uk.gov.justice.services.core.extension;

import uk.gov.justice.domain.annotation.Event;

/**
 * Event representing the occurrence of a class with an {@link Event} annotation having been
 * identified by the framework.
 */
public interface EventFoundEvent {

    Class<?> getClazz();

    String getEventName();
}
