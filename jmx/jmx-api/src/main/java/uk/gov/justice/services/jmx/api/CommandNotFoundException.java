package uk.gov.justice.services.jmx.api;

public class CommandNotFoundException extends RuntimeException {

    public CommandNotFoundException(final String message) {
        super(message);
    }
}
