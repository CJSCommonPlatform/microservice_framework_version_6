package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Dispatches messages to their corresponding handlers, which could be a command handler, command
 * controller, event processor, etc.
 *
 * This class handles both synchronous and asynchronous dispatching.
 */
public interface Dispatcher {

    /**
     * Dispatch message to its corresponding handler, which could be a command handler, command
     * controller, event processor, etc.
     *
     * @param envelope the envelope to dispatch to a handler
     * @return the envelope returned by the handler method
     */
    JsonEnvelope dispatch(final JsonEnvelope envelope);

    /**
     * Registers the handler instance.
     *
     * Called by an Observer to populate the handler
     * registry.
     *
     * @param handler handler instance to be registered.
     */
    void register(final Object handler);
}
