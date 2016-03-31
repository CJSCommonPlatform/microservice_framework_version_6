package uk.gov.justice.services.core.handler;

import static java.lang.String.format;

import uk.gov.justice.services.core.handler.exception.HandlerExecutionException;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.Envelope;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wrapper class for executing a specific synchronous handling method on a command handler instance.
 */
public class SynchronousHandlerMethod extends HandlerMethod {

    public SynchronousHandlerMethod(Object handlerInstance, Method handlerMethod) {
        super(handlerInstance, handlerMethod);

        if (!Envelope.class.equals(handlerMethod.getReturnType())) {
            throw new InvalidHandlerException("Synchronous handler must return an envelope");
        }
    }

    /**
     * Invokes the handler method passing the <code>envelope</code> to it.
     *
     * @param envelope the envelope that is passed to the handler method
     * @return the result of invoking the handler
     */
    public Envelope execute(final Envelope envelope) {
        try {
            return (Envelope) handlerMethod.invoke(handlerInstance, envelope);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new HandlerExecutionException(format("Error while invoking command handler method %s with parameter %s", handlerMethod, envelope), e);
        }
    }
}
