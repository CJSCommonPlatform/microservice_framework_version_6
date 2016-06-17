package uk.gov.justice.services.generators.commons.validator;

public class RamlValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RamlValidationException(final String message) {
        super(message);
    }

    public RamlValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
