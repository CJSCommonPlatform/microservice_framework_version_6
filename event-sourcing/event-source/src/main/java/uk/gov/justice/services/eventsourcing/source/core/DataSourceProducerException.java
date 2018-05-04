package uk.gov.justice.services.eventsourcing.source.core;

/**
 * Exception thrown when there's error accessing database
 */
public class DataSourceProducerException extends RuntimeException {

    private static final long serialVersionUID = 5934757152541630746L;

    public DataSourceProducerException(final String message) {
        super(message);
    }
}
