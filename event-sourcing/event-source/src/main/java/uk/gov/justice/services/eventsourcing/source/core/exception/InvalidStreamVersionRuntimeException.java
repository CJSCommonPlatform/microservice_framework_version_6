package uk.gov.justice.services.eventsourcing.source.core.exception;

/**
 * RuntimeException raised when an invalid version is specified.
 */
public class InvalidStreamVersionRuntimeException extends RuntimeException {

    public InvalidStreamVersionRuntimeException(final String message) {
        super(message);
    }

}
