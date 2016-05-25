package uk.gov.justice.services.core.dispatcher;

import static uk.gov.justice.services.core.handler.HandlerMethod.ASYNCHRONOUS;
import static uk.gov.justice.services.core.handler.HandlerMethod.SYNCHRONOUS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.handler.HandlerMethod;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;
import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Dispatches messages synchronously or asynchronously to their corresponding handlers, which could
 * be a command handler, command controller, event processor, etc.
 *
 * This class handles both synchronous and asynchronous dispatching. Note that it does not implement
 * the {{@link SynchronousDispatcher} or {@link AsynchronousDispatcher} interfaces. This is because
 * the <code>dispatch</code> method names would clash. Instead, we expose the dispatcher as a
 * functional interface via the {@link DispatcherProducer}.
 */
class Dispatcher {

    private HandlerRegistry handlerRegistry;

    private static final Logger LOGGER = LoggerFactory.getLogger(Dispatcher.class);

    Dispatcher() {
        handlerRegistry = new HandlerRegistry();
    }

    /**
     * Asynchronously dispatch message to its corresponding handler, which could be a command
     * handler, command controller, event processor, etc.
     *
     * The underlying {@link HandlerMethod} will have returned a null {@link Void}, which we throw
     * away at this point to provide void method that can be exposed via the {@link
     * AsynchronousDispatcher} interface.
     *
     * @param envelope the envelope to dispatch to a handler
     */
    void asynchronousDispatch(final JsonEnvelope envelope) {
        getMethod(envelope, ASYNCHRONOUS).execute(envelope);
    }

    /**
     * Synchronously dispatch message to its corresponding handler, which could be a command
     * handler, command controller, event processor, etc.
     *
     * @param envelope the envelope to dispatch to a handler
     * @return the envelope returned by the handler method
     */
    JsonEnvelope synchronousDispatch(final JsonEnvelope envelope) {
        return (JsonEnvelope) getMethod(envelope, SYNCHRONOUS).execute(envelope);
    }

    /**
     * Registers the handler instance.
     *
     * This is only called by the {@link DispatcherProducer} to populate the handler registry.
     *
     * @param handler handler instance to be registered.
     */
    void register(final Object handler) {
        handlerRegistry.register(handler);
    }

    /**
     * Get the handler method for handling this envelope or throw an exception.
     *
     * @param envelope the envelope to be handled
     * @return the handler method
     */
    private HandlerMethod getMethod(final JsonEnvelope envelope, final boolean isSynchronous) {
        final String name = envelope.metadata().name();
        return handlerRegistry.get(name, isSynchronous);
    }
}
