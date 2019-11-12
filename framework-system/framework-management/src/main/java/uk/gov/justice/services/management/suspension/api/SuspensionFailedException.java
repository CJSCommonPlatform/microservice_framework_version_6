package uk.gov.justice.services.management.suspension.api;

public class SuspensionFailedException extends Exception {

    public SuspensionFailedException(final String message) {
        super(message);
    }
}
