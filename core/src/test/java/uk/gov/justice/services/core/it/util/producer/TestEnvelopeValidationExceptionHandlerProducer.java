package uk.gov.justice.services.core.it.util.producer;


import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandler;
import uk.gov.justice.services.core.envelope.RethrowingValidationExceptionHandler;

import javax.enterprise.inject.Produces;

public class TestEnvelopeValidationExceptionHandlerProducer {

    private static final EnvelopeValidationExceptionHandler VALIDATION_EXCEPTION_HANDLER
            = new RethrowingValidationExceptionHandler();

    @Produces
    public EnvelopeValidationExceptionHandler envelopeValidationExceptionHandler() {
        return VALIDATION_EXCEPTION_HANDLER;
    }

}
