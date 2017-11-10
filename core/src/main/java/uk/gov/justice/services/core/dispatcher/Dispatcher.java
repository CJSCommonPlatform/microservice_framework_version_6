package uk.gov.justice.services.core.dispatcher;

import static java.lang.String.format;

import uk.gov.justice.services.core.handler.HandlerMethod;
import uk.gov.justice.services.core.handler.exception.HandlerExecutionException;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.IOException;

import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Dispatches messages to their corresponding handlers, which could be a command handler, command
 * controller, event processor, etc.
 *
 * This class handles both synchronous and asynchronous dispatching.
 */
public class Dispatcher {

    private final HandlerRegistry handlerRegistry;
    private final ObjectMapper objectMapper;

    public Dispatcher(final HandlerRegistry handlerRegistry, final ObjectMapper objectMapper) {
        this.handlerRegistry = handlerRegistry;
        this.objectMapper = objectMapper;
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
        final Class<?> envelopeGenericType = handlerMethod.getEnvelopeGenericType();

        if(envelopeGenericType == JsonEnvelope.class) {
            return (JsonEnvelope) handlerMethod.execute(envelope);
        }

        final JsonObject jsonObject = envelope.payloadAsJsonObject();
        final Object readValue;

        try {
            readValue = objectMapper.readValue(jsonObject.toString(), envelopeGenericType);
        } catch (IOException e) {
            throw new HandlerExecutionException(
                    format("Error while invoking handler method %s with parameter %s",
                            handlerMethod, envelope), e.getCause());
        }

        final Envelope<Object> pojoEnvelope = (Envelope.envelopeFrom(envelope.metadata(), readValue));

        return (JsonEnvelope) handlerMethod.execute(pojoEnvelope);
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
