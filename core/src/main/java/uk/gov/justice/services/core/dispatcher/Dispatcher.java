package uk.gov.justice.services.core.dispatcher;

import static java.lang.String.format;

import uk.gov.justice.services.core.handler.HandlerMethod;
import uk.gov.justice.services.core.handler.exception.HandlerExecutionException;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.IOException;

import javax.json.JsonValue;

/**
 * Dispatches messages to their corresponding handlers, which could be a command handler, command
 * controller, event processor, etc.
 *
 * This class handles both synchronous and asynchronous dispatching.
 */
public class Dispatcher {

    private final HandlerRegistry handlerRegistry;
    private final EnvelopeTypeConverter typeConverter;
    private final JsonEnvelopeConverter jsonEnvelopeConverter;

    public Dispatcher(final HandlerRegistry handlerRegistry,
                      final EnvelopeTypeConverter typeConverter,
                      final JsonEnvelopeConverter jsonEnvelopeConverter) {
        this.handlerRegistry = handlerRegistry;
        this.typeConverter = typeConverter;
        this.jsonEnvelopeConverter = jsonEnvelopeConverter;
    }

    /**
     * Dispatch message to its corresponding handler, which could be a command handler, command
     * controller, event processor, etc.
     *
     * @param envelope the envelope to dispatch to a handler
     * @return the envelope returned by the handler method
     */
    public JsonEnvelope dispatch(final JsonEnvelope envelope) {

        final HandlerMethod handlerMethod = handlerRegistry.get(envelope.metadata().name());
        final Class<?> payloadClass = handlerMethod.getEnvelopeParameterType();

        try {
            return toJsonEnvelope(
                    toJsonValueEnvelope(
                            (Envelope<?>) handlerMethod.execute(
                                    toTargetEnvelopType(envelope, payloadClass))));

        } catch (IOException e) {
            throw new HandlerExecutionException(
                    format("Error while invoking handler method %s with parameter %s",
                            handlerMethod, envelope), e.getCause());
        }
    }

    private Envelope<?> toTargetEnvelopType(JsonEnvelope envelope, Class<?> payloadClass) throws IOException {
        return typeConverter.convert(envelope, payloadClass);
    }

    private JsonEnvelope toJsonEnvelope(Envelope<JsonValue> envelope) {
        return jsonEnvelopeConverter.toJsonEnvelope(envelope);
    }

    private Envelope<JsonValue> toJsonValueEnvelope(Envelope<?> envelope) throws IOException {
        return typeConverter.convert(envelope, JsonValue.class);
    }


    /**
     * Registers the handler instance.
     *
     * Called by an Observer to populate the handler
     * registry.
     *
     * @param handler handler instance to be registered.
     */
    public void register(final Object handler) {
        handlerRegistry.register(handler);
    }
}
