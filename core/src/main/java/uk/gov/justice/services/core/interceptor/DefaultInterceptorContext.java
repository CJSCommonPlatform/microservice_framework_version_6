package uk.gov.justice.services.core.interceptor;

import static uk.gov.justice.services.core.interceptor.DefaultContextPayload.copyWithEnvelope;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

public class DefaultInterceptorContext implements InterceptorContext {

    private static final String UNKNOWN_COMPONENT = "UNKNOWN";

    private final ContextPayload input;
    private final ContextPayload output;

    /**
     * Construct an InterceptorContext that contains an input {@link ContextPayload}, an output
     * {@link ContextPayload}, and an injection point of interceptor chain process.
     *
     * @param input  the input ContextPayload
     * @param output the output ContextPayload
     */
    public DefaultInterceptorContext(final ContextPayload input, final ContextPayload output) {
        this.input = input;
        this.output = output;
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

    @Override
    public String getComponentName() {
        return (String) getInputParameter("component").orElse(UNKNOWN_COMPONENT);
    }
}
