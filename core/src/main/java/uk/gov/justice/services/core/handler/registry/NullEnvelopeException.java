package uk.gov.justice.services.core.handler.registry;

public class NullEnvelopeException extends RuntimeException {
    public NullEnvelopeException(String message, Throwable cause) {
        super(message, cause);
    }
}
