package uk.gov.justice.services.core.interceptor;

import static uk.gov.justice.services.core.interceptor.DefaultContextPayload.contextPayloadWith;
import static uk.gov.justice.services.core.interceptor.DefaultContextPayload.contextPayloadWithNoEnvelope;
import static uk.gov.justice.services.core.interceptor.DefaultContextPayload.copyWithEnvelope;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

public class DefaultInterceptorContext implements InterceptorContext {

    private final ContextPayload input;
    private final ContextPayload output;

    /**
     * Construct an InterceptorContext that contains an input {@link ContextPayload}, an output
     * {@link ContextPayload}, and an injection point of interceptor chain process.
     *
     * @param input  the input ContextPayload
     * @param output the output ContextPayload
     */
    private DefaultInterceptorContext(final ContextPayload input, final ContextPayload output) {
        this.input = input;
        this.output = output;
    }

    /**
     * Create an interceptor context with an input {@link ContextPayload} that wraps the input
     * envelope, an output {@link ContextPayload} with no envelope and an injection point.
     *
     * @param input the input JsonEnvelope
     * @return the new InterceptorContext
     */
    public static InterceptorContext interceptorContextWithInput(final JsonEnvelope input) {
        contextPayloadWith(input);
        return new DefaultInterceptorContext(contextPayloadWith(input), contextPayloadWithNoEnvelope());
    }

    public InterceptorContext copyWithInput(final JsonEnvelope inputEnvelope) {
        return new DefaultInterceptorContext(
                copyWithEnvelope(this.inputContext(), inputEnvelope),
                this.outputContext());
    }

    public InterceptorContext copyWithOutput(final JsonEnvelope outputEnvelope) {
        return new DefaultInterceptorContext(
                this.inputContext(),
                copyWithEnvelope(this.outputContext(), outputEnvelope));
    }

    public JsonEnvelope inputEnvelope() {
        return input.getEnvelope().orElseThrow(() -> new IllegalStateException("No input envelope set."));
    }

    public Optional<JsonEnvelope> outputEnvelope() {
        return output.getEnvelope();
    }

    public Optional<Object> getInputParameter(final String name) {
        return input.getParameter(name);
    }

    public void setInputParameter(final String name, final Object parameter) {
        input.setParameter(name, parameter);
    }

    public Optional<Object> getOutputParameter(final String name) {
        return output.getParameter(name);
    }

    public void setOutputParameter(final String name, final Object parameter) {
        output.setParameter(name, parameter);
    }

    public ContextPayload inputContext() {
        return input;
    }

    public ContextPayload outputContext() {
        return output;
    }
}
