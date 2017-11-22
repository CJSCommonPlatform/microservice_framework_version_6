package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.core.handler.HandlerMethod;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;
import uk.gov.justice.services.core.handler.registry.NullEnvelopeException;
import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Dispatches messages to their corresponding handlers, which could be a command handler, command
 * controller, event processor, etc.
 *
 * This class handles both synchronous and asynchronous dispatching.
 */
public class Dispatcher {

    private final HandlerRegistry handlerRegistry;
    private final EnvelopePayloadTypeConverter typeConverter;
    private final JsonEnvelopeRepacker jsonEnvelopeRepacker;

    public Dispatcher(final HandlerRegistry handlerRegistry,
                      final EnvelopePayloadTypeConverter typeConverter,
                      final JsonEnvelopeRepacker jsonEnvelopeRepacker) {
        this.handlerRegistry = handlerRegistry;
        this.typeConverter = typeConverter;
        this.jsonEnvelopeRepacker = jsonEnvelopeRepacker;
    }

    /**
     * Dispatch message to its corresponding handler, which could be a command handler, command
     * controller, event processor, etc.
     *
     * @param envelope the envelope to dispatch to a handler
     * @return the envelope returned by the handler method
     */
    public JsonEnvelope dispatch(final JsonEnvelope envelope) {

        final HandlerMethod handlerMethod;
        try {
            handlerMethod = handlerRegistry.get(envelope.metadata().name());
        } catch (NullPointerException e) {
            throw new NullEnvelopeException("Envelope cannot be null", e);
        }

        return MethodInvoker
                .createMethodInvoker(typeConverter, jsonEnvelopeRepacker)
                .invoke(handlerMethod, envelope);
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
