package uk.gov.justice.services.core.envelope;

import javax.enterprise.inject.Alternative;

@Alternative
public class RethrowingValidationExceptionHandler implements EnvelopeValidationExceptionHandler {
    @Override
    public void handle(final EnvelopeValidationException ex) {
        throw ex;
    }
}
