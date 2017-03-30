package uk.gov.justice.services.eventsourcing.source.core.exception;

/**
 * Exception raised when version mismatch occurs during append.
 */
public class VersionMismatchException extends EventStreamException {

    private static final long serialVersionUID = -3036499309662967526L;

    public VersionMismatchException(final String message) {
        super(message);
    }

}
