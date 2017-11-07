package uk.gov.justice.services.messaging.spi;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;

/**
 * Default implementation of an envelope.
 */
public class DefaultEnvelope <T> implements Envelope<T> {

    private final Metadata metadata;

    private final T payload;

    DefaultEnvelope(final Metadata metadata, final T payload) {
        this.metadata = metadata;
        this.payload = payload;
    }

    @Override
    public Metadata metadata() {
        return metadata;
    }

    @Override
    public T payload() {
        return payload;
    }
}
