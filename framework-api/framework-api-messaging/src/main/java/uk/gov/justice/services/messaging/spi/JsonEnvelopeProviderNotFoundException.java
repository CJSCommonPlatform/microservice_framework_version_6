package uk.gov.justice.services.messaging.spi;

public class JsonEnvelopeProviderNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -9004536297548965775L;

    public JsonEnvelopeProviderNotFoundException(final String message) {
        super(message);
    }
}