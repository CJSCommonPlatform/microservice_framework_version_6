package uk.gov.justice.services.eventsourcing.repository.core.exception;

public class StoreEventRequestFailedException extends Exception {

    public StoreEventRequestFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
