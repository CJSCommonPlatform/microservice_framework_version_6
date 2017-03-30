package uk.gov.justice.services.eventsourcing.repository.core.exception;

public class StoreEventRequestFailedException extends Exception {

    private static final long serialVersionUID = 764160955757553538L;

    public StoreEventRequestFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
