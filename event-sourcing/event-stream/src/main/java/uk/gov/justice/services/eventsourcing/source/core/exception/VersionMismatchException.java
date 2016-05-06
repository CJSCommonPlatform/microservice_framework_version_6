package uk.gov.justice.services.eventsourcing.source.core.exception;

/**
 * Exception raised when version mismatch occurs during append.
 */
public class VersionMismatchException extends EventStreamException {

    public VersionMismatchException(final String message) {
        super(message);
    }

}
