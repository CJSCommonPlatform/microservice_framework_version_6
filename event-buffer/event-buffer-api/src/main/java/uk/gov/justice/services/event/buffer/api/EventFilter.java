package uk.gov.justice.services.event.buffer.api;

public interface EventFilter {
    boolean accepts(final String eventName);
}
