package uk.gov.justice.services.eventsourcing.publishing;

public class PublishQueueException extends RuntimeException {

    public PublishQueueException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
