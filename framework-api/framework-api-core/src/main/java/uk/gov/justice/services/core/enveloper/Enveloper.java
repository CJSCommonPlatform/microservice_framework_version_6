package uk.gov.justice.services.core.enveloper;

import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.function.Function;

import javax.enterprise.event.Observes;

public interface Enveloper {

    /**
     * Register method, invoked automatically to register all event classes into the eventMap.
     *
     * @param event identified by the framework to be registered into the event map.
     */
    void register(@Observes final EventFoundEvent event);

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
