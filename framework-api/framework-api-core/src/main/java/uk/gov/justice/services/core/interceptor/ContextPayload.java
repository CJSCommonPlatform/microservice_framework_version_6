package uk.gov.justice.services.core.interceptor;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Map;
import java.util.Optional;

/**
 * Contains an input or output envelope and any associated parameters for interceptor processing.
 */
public interface ContextPayload {

    /**
     * Return the {@link JsonEnvelope} that is part of the Context Payload.  Returns {@link
     * Optional#empty()} if no JsonEnvelope is present.
     *
     * @return Optional JsonEnvelope
     */
    Optional<JsonEnvelope> getEnvelope();

    /**
     * Get the parameter object value for a given parameter name.  Returns {@link Optional#empty()}
     * if no object value present.
     *
     * @param name parameter key name
     * @return Optional parameter value
     */
    Optional<Object> getParameter(final String name);

    /**
     * Set a parameter using the name as the key to the Object parameter.
     *
     * @param name      parameter key name
     * @param parameter parameter object value
     */
    void setParameter(final String name, final Object parameter);

    /**
     * Returns a copy of the parameter map associated with this Context Payload
     *
     * @return the Map of key value to parameter object value
     */
    Map<String, Object> copyOfParameters();
}
