package uk.gov.justice.services.core.aggregate.exception;

public class AggregateCreationException extends RuntimeException {

    public AggregateCreationException(final String message, final Exception e) {
        super(message, e);
    }
}
