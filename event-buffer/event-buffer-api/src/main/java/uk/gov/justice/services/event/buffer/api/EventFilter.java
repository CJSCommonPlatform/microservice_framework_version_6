package uk.gov.justice.services.event.buffer.api;

import java.util.Set;

public interface EventFilter {

    boolean accepts(final String eventName);

    Set<String> getSupportedEvents();
}
