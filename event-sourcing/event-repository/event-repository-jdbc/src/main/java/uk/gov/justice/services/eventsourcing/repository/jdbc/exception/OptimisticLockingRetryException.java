package uk.gov.justice.services.eventsourcing.repository.jdbc.exception;

/**
 * Exception thrown when the event log repository insertion fails due to a unique constraint
 * violation. This exception is manually thrown to allow the framework to retry the insertion within
 * the current transaction.
 */
public class OptimisticLockingRetryException extends RuntimeException {

    private static final long serialVersionUID = 5934757852545630746L;

    public OptimisticLockingRetryException(final String message) {
        super(message);
    }
}
