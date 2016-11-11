package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.core.handler.registry.HandlerRegistry;
import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Dispatches messages to their corresponding handlers, which could be a command handler, command
 * controller, event processor, etc.
 *
 * This class handles both synchronous and asynchronous dispatching.
 */
public class DefaultDispatcher implements Dispatcher {

    private final HandlerRegistry handlerRegistry;

    public DefaultDispatcher(final HandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    public JsonEnvelope dispatch(final JsonEnvelope envelope) {
        return (JsonEnvelope) handlerRegistry.get(envelope.metadata().name()).execute(envelope);
    }

    public void register(final Object handler) {
        handlerRegistry.register(handler);
    }
}
