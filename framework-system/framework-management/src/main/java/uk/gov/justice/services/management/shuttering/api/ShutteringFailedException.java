package uk.gov.justice.services.management.shuttering.api;

public class ShutteringFailedException extends Exception {

    public ShutteringFailedException(final String message) {
        super(message);
    }
}
