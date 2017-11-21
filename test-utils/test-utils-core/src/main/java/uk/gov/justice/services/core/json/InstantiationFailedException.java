package uk.gov.justice.services.core.json;

public class InstantiationFailedException extends RuntimeException {

    public InstantiationFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
