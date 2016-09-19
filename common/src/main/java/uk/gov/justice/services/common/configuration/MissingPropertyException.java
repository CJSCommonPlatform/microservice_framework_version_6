package uk.gov.justice.services.common.configuration;

public class MissingPropertyException extends RuntimeException {

    private static final long serialVersionUID = -2539036250375591049L;

    public MissingPropertyException(final String message) {
        super(message);
    }

}
