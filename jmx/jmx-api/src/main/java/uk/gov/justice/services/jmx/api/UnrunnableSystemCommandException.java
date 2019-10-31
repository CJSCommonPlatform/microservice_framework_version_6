package uk.gov.justice.services.jmx.api;

public class UnrunnableSystemCommandException extends RuntimeException {

    public UnrunnableSystemCommandException(final String message) {
        super(message);
    }
}
