package uk.gov.justice.raml.jms.core;

public class JmsEndpointGeneratorException extends RuntimeException {
    public JmsEndpointGeneratorException(final String message, final Throwable e) {
        super(message, e);
    }
}
