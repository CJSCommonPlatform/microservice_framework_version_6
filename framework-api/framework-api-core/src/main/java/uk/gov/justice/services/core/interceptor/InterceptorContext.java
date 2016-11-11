package uk.gov.justice.services.core.interceptor;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.enterprise.inject.spi.InjectionPoint;

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

    JsonEnvelope inputEnvelope();

    Optional<JsonEnvelope> outputEnvelope();

    Optional<Object> getInputParameter(final String name);

    void setInputParameter(final String name, final Object parameter);

    Optional<Object> getOutputParameter(final String name);

    void setOutputParameter(final String name, final Object parameter);

    ContextPayload inputContext();

    ContextPayload outputContext();
}
