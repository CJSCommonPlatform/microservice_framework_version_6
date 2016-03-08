package uk.gov.justice.services.messaging;

import javax.json.JsonObject;

/**
 * Default implementation of an envelope.
 */
public class DefaultEnvelope implements Envelope {

    private Metadata metadata;

    private JsonObject payload;

    private DefaultEnvelope(final Metadata metadata, final JsonObject payload) {
        this.metadata = metadata;
        this.payload = payload;
    }

    public static Envelope envelopeFrom(final Metadata metadata, final JsonObject payload) {
        return new DefaultEnvelope(metadata, payload);
    }

    @Override
    public Metadata metadata() {
        return metadata;
    }

    @Override
    public JsonObject payload() {
        return payload;
    }

}
