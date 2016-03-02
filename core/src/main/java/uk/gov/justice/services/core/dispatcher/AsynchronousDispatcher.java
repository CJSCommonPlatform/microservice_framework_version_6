package uk.gov.justice.services.core.dispatcher;


import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;
import uk.gov.justice.services.messaging.Envelope;

import javax.enterprise.inject.Alternative;

/**
 * Dispatches messages asynchronously to their corresponding handlers, which could be a command handler,
 * command controller, event processor, etc.
 */
@Alternative
public class AsynchronousDispatcher implements Dispatcher {

    private HandlerRegistry handlerRegistry;

    public AsynchronousDispatcher() {
        this.handlerRegistry = new HandlerRegistry();
    }

    /**
     * Dispatch message to its corresponding handler, which could be a command handler,
     * command controller, event processor, etc.
     *
     * @param envelope envelope as jsonObject
     */
    @Override
    public void dispatch(final Envelope envelope) {

        final String name = envelope.metadata().name();

        if (handlerRegistry.canHandle(name)) {
            handlerRegistry.get(name).execute(envelope);
        } else {
            throw new MissingHandlerException("No handler registered to handle action :" + name);
        }

    }

    /**
     * Registers the handler instance.
     *
     * @param handler Handler instance to be registered.
     */
    void register(final Object handler) {
        handlerRegistry.register(handler);
    }
}
