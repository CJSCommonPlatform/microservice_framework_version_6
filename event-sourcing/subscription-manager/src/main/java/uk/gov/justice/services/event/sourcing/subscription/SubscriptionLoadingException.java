package uk.gov.justice.services.event.sourcing.subscription;

public class SubscriptionLoadingException extends RuntimeException {

    public SubscriptionLoadingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
