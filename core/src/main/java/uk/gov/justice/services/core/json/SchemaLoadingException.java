package uk.gov.justice.services.core.json;


public class SchemaLoadingException extends RuntimeException {

    public SchemaLoadingException(final String message) {
        super(message);
    }

    public SchemaLoadingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
