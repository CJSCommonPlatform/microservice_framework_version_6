package uk.gov.justice.subscription.registry;

public class RegistryException extends RuntimeException {

    public RegistryException(final String message) {
        super(message);
    }

    public RegistryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
