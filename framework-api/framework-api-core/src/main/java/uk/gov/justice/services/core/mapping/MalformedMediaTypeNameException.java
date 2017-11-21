package uk.gov.justice.services.core.mapping;

public class MalformedMediaTypeNameException extends RuntimeException {

    private static final long serialVersionUID = 5426383027375213199L;

    public MalformedMediaTypeNameException(final String message) {
        super(message);
    }
}
