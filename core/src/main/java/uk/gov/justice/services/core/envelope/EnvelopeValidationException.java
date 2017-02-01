package uk.gov.justice.services.core.envelope;

public class EnvelopeValidationException extends RuntimeException {

    public EnvelopeValidationException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public EnvelopeValidationException(final String message) {
        super(message);
    }
}
