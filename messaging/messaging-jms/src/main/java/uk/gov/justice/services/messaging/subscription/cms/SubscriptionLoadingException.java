package uk.gov.justice.services.messaging.subscription.cms;

public class SubscriptionLoadingException extends RuntimeException {

    public SubscriptionLoadingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
