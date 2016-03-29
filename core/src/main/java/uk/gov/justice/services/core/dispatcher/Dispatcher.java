package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.core.handler.AsynchronousHandlerMethod;
import uk.gov.justice.services.core.handler.SynchronousHandlerMethod;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;
import uk.gov.justice.services.messaging.Envelope;

import javax.enterprise.inject.Alternative;

/**
 * Dispatches messages synchronously or asynchronously to their corresponding handlers, which could
 * be a command handler, command controller, event processor, etc.
 *
 * This class handles both synchronous and asynchronous dispatching. Note that it does not
 * implement the {{@link SynchronousDispatcher} or {@link AsynchronousDispatcher} interfaces. This
 * is because the <code>dispatch</code> method names would clash. Instead, we expose the dispatcher
 * as a functional interface via the {@link DispatcherProducer}.
 */
class Dispatcher {

    private HandlerRegistry handlerRegistry;

    Dispatcher() {
        handlerRegistry = new HandlerRegistry();
    }

    /**
     * Asynchronously dispatch message to its corresponding handler, which could be a command
     * handler, command controller, event processor, etc.
     *
     * @param envelope the envelope to dispatch to a handler
     */
    void asynchronousDispatch(final Envelope envelope) {
        getAsynchronousMethod(envelope).execute(envelope);
    }

    /**
     * Synchronously dispatch message to its corresponding handler, which could be a command
     * handler, command controller, event processor, etc.
     *
     * @param envelope the envelope to dispatch to a handler
     * @return the envelope returned by the handler method
     */
    Envelope synchronousDispatch(final Envelope envelope) {
        return getSynchronousMethod(envelope).execute(envelope);
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
    private AsynchronousHandlerMethod getAsynchronousMethod(final Envelope envelope) {

        final String name = envelope.metadata().name();

        if (handlerRegistry.canHandleAsynchronous(name)) {
            return handlerRegistry.getAsynchronous(name);
        } else {
            throw new MissingHandlerException("No handler registered to handle action: " + name);
        }
    }

    /**
     * Get the handler method for handling this envelope or throw an exception.
     *
     * @param envelope the envelope to be handled
     * @return the handler method
     */
    private SynchronousHandlerMethod getSynchronousMethod(final Envelope envelope) {

        final String name = envelope.metadata().name();

        if (handlerRegistry.canHandleSynchronous(name)) {
            return handlerRegistry.getSynchronous(name);
        } else {
            throw new MissingHandlerException("No handler registered to handle action: " + name);
        }
    }
}
