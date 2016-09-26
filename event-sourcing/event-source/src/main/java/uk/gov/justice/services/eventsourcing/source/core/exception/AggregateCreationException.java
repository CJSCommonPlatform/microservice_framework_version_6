package uk.gov.justice.services.eventsourcing.source.core.exception;

public class AggregateCreationException extends RuntimeException {

    public AggregateCreationException(final String message, final Exception e) {
        super(message, e);
    }
}
