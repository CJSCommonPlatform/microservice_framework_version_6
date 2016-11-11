package uk.gov.justice.services.core.eventfilter;

public interface EventFilter {
    boolean accepts(final String eventName);
}
