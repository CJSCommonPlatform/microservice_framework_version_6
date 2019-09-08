package uk.gov.justice.services.jmx.api;

public class UnsupportedSystemCommandException extends RuntimeException {

    public UnsupportedSystemCommandException(final String message) {
        super(message);
    }
}
