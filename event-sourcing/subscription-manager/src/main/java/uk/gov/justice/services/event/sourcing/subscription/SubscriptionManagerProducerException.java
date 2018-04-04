package uk.gov.justice.services.event.sourcing.subscription;

public class SubscriptionManagerProducerException extends RuntimeException {

    public SubscriptionManagerProducerException(final String message) {
        super(message);
    }
}
