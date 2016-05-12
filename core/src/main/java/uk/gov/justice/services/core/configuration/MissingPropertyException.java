package uk.gov.justice.services.core.configuration;

public class MissingPropertyException extends RuntimeException {

    public MissingPropertyException(final String message) {
        super(message);
    }

}
