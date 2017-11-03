package uk.gov.justice.services.messaging.spi;

public class EnvelopeProviderNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -9004536297548965775L;

    public EnvelopeProviderNotFoundException(final String message) {
        super(message);
    }
}