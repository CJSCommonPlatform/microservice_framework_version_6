package uk.gov.justice.services.core.mapping;

public class MalformedMediaTypeException extends RuntimeException {

    private static final long serialVersionUID = -7396471821477969237L;

    public MalformedMediaTypeException(final String message) {
        super(message);
    }
}
