package uk.gov.justice.services.core.interceptor;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

public interface InterceptorContext {

    /**
     * Create a copy of this interceptor context that will contain the provided input
     * envelope.
     *
     * @param inputEnvelope the inputEnvelope JsonEnvelope to set as input
     * @return the new InterceptorContext
     */
    InterceptorContext copyWithInput(final JsonEnvelope inputEnvelope);

    /**
     * Create a copy of this interceptor context that will contain the provided output
     * envelope.
     *
     * @param outputEnvelope the outputEnvelope JsonEnvelope to set as the output
     * @return the new InterceptorContext
     */
    InterceptorContext copyWithOutput(final JsonEnvelope outputEnvelope);

    /**
     * Convenient access method for getting the input JsonEnvelope from the Input Context
     *
     * @return the input JsonEnvelope
     */
    JsonEnvelope inputEnvelope();

    /**
     * Convenient access method for getting the output JsonEnvelope from the Output Context
     *
     * @return Optional output JsonEnvelope
     */
    Optional<JsonEnvelope> outputEnvelope();

    /**
     * Convenient access method for getting an input parameter.
     *
     * @param name parameter name key
     * @return Optional parameter object value
     */
    Optional<Object> getInputParameter(final String name);

    /**
     * Convenient access method for setting an input parameter.
     *
     * @param name      parameter name key
     * @param parameter parameter object value
     */
    void setInputParameter(final String name, final Object parameter);

    /**
     * Convenient access method for getting an output parameter.
     *
     * @param name parameter name key
     * @return Optional parameter object value
     */
    Optional<Object> getOutputParameter(final String name);

    /**
     * Convenient access method for setting an output parameter.
     *
     * @param name      parameter name key
     * @param parameter parameter object value
     */
    void setOutputParameter(final String name, final Object parameter);

    ContextPayload inputContext();

    ContextPayload outputContext();
}
