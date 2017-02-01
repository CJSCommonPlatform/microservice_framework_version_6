package uk.gov.justice.services.core.envelope;

public class RethrowingValidationExceptionHandler implements EnvelopeValidationExceptionHandler {
    @Override
    public void handle(final EnvelopeValidationException ex) {
        throw ex;
    }
}
