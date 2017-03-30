package uk.gov.justice.services.core.interceptor;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contains an input or output envelope and any associated parameters for interceptor processing.
 */
public class DefaultContextPayload implements ContextPayload {

    private final Optional<JsonEnvelope> envelope;
    private final Map<String, Object> contextParameters;

    private DefaultContextPayload() {
        this.envelope = Optional.empty();
        this.contextParameters = new HashMap<>();
    }

    private DefaultContextPayload(final JsonEnvelope envelope) {
        this.envelope = Optional.ofNullable(envelope);
        this.contextParameters = new HashMap<>();
    }

    private DefaultContextPayload(final JsonEnvelope envelope, final Map<String, Object> contextParameters) {
        this.envelope = Optional.ofNullable(envelope);
        this.contextParameters = contextParameters;
    }

    /**
     * Create a blank context payload with no envelope or parameters set
     *
     * @return a context payload
     */
    public static ContextPayload contextPayloadWithNoEnvelope() {
        return new DefaultContextPayload();
    }

    /**
     * Create a context payload that will contain the provided envelope
     *
     * @param envelope the envelope to set
     * @return a context payload containing the envelope
     */
    public static ContextPayload contextPayloadWith(final JsonEnvelope envelope) {
        return new DefaultContextPayload(envelope);
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
        return new DefaultContextPayload(envelope, contextPayload.copyOfParameters());
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

    public Map<String, Object> copyOfParameters() {
        return new HashMap<>(contextParameters);
    }
}
