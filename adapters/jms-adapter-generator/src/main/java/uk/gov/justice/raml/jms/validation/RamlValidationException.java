package uk.gov.justice.raml.jms.validation;

public class RamlValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public RamlValidationException(final String message) {
        super(message);
    }

}
