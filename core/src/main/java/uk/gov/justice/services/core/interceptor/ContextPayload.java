package uk.gov.justice.services.core.interceptor;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contains an input or output envelope and any associated parameters for interceptor processing.
 */
public class ContextPayload {

    private final Optional<JsonEnvelope> envelope;
    private final Map<String, Object> contextParameters = new HashMap<>();

    private ContextPayload() {
        this.envelope = Optional.empty();
    }

    private ContextPayload(final JsonEnvelope envelope) {
        this.envelope = Optional.ofNullable(envelope);
    }

    /**
     * Create a blank context payload with no envelope or parameters set
     *
     * @return a context payload
     */
    public static ContextPayload contextPayloadWithNoEnvelope() {
        return new ContextPayload();
    }

    /**
     * Create a context payload that will contain the provided envelope
     *
     * @param envelope the envelope to set
     * @return a context payload containing the envelope
     */
    public static ContextPayload contextPayloadWith(final JsonEnvelope envelope) {
        return new ContextPayload(envelope);
    }

    /**
     * Create a context payload with a copy of the parameters from the provided context payload,
     * that will contain the provided envelope.
     *
     * @param contextPayload the context payload to copy parameters from
     * @param envelope       the envelope to set
     * @return a context payload
     */
    public static ContextPayload copyWithEnvelope(final ContextPayload contextPayload, final JsonEnvelope envelope) {
        final ContextPayload copy = new ContextPayload(envelope);
        copy.contextParameters.putAll(contextPayload.contextParameters);
        return copy;
    }

    public Optional<JsonEnvelope> getEnvelope() {
        return envelope;
    }

    public Optional<Object> getParameter(final String name) {
        return Optional.ofNullable(contextParameters.get(name));
    }

    public void setParameter(final String name, final Object parameter) {
        contextParameters.put(name, parameter);
    }
}
