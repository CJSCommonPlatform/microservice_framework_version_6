package uk.gov.justice.services.core.enveloper;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.function.Function;

public interface Enveloper {

    /**
     * Provides a function that wraps the provided object into a new {@link JsonEnvelope} using
     * metadata from the given envelope.
     *
     * @param envelope - the envelope containing source metadata.
     * @return a function that wraps objects into an envelope.
     */
    Function<Object, JsonEnvelope> withMetadataFrom(final JsonEnvelope envelope);

    /**
     * Provides a function that wraps the provided object into a new {@link JsonEnvelope} using
     * metadata from the given envelope, except the name.
     *
     * @param envelope - the envelope containing source metadata.
     * @param name     - name of the payload.
     * @return a function that wraps objects into an envelope.
     */
    Function<Object, JsonEnvelope> withMetadataFrom(final JsonEnvelope envelope, final String name);
}
