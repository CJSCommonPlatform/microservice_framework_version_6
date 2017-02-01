package uk.gov.justice.services.core.envelope;

public interface EnvelopeValidationExceptionHandler {

    void handle(final EnvelopeValidationException ex);
}
