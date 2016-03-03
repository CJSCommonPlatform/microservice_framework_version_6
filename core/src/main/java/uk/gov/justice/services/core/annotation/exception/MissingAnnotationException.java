package uk.gov.justice.services.core.annotation.exception;

public class MissingAnnotationException extends RuntimeException {

    public MissingAnnotationException(final String message) {
        super(message);
    }

}
