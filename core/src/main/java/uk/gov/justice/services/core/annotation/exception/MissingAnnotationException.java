package uk.gov.justice.services.core.annotation.exception;

public class MissingAnnotationException extends RuntimeException {

    private static final long serialVersionUID = 2062445631409937617L;

    public MissingAnnotationException(final String message) {
        super(message);
    }

}
