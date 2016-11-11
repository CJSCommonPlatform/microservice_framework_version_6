package uk.gov.justice.services.eventsourcing.source.core.exception;

/**
 * RuntimeException raised when an invalid version is specified.
 */
public class InvalidStreamVersionRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 8080741742871998984L;

    public InvalidStreamVersionRuntimeException(final String message) {
        super(message);
    }

}
