package uk.gov.justice.raml.jms.core;

public class JmsEndpointGeneratorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public JmsEndpointGeneratorException(final String message, final Throwable e) {
        super(message, e);
    }

}
