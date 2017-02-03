package uk.gov.justice.services.core.envelope;

import javax.inject.Inject;

import org.slf4j.Logger;

public class LoggingValidationExceptionHandler implements EnvelopeValidationExceptionHandler {

    @Inject
    private Logger logger;

    @Override
    public void handle(final EnvelopeValidationException ex) {
        logger.warn("Message validation failed", ex);
    }
}
