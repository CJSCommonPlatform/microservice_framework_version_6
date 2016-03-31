package uk.gov.justice.services.core.handler;

import static java.lang.String.format;

import uk.gov.justice.services.core.handler.exception.HandlerExecutionException;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.Envelope;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wrapper class for executing a specific asynchronous handling method on a command handler instance.
 */
public class AsynchronousHandlerMethod extends HandlerMethod {

    public AsynchronousHandlerMethod(Object handlerInstance, Method handlerMethod) {
        super(handlerInstance, handlerMethod);

        if (!Void.TYPE.equals(handlerMethod.getReturnType())) {
            throw new InvalidHandlerException("Asynchronous handler must return void");
        }
    }

    /**
     * Invokes the handler method passing the <code>envelope</code> to it.
     *
     * @param envelope the envelope that is passed to the handler method
     */
    public void execute(final Envelope envelope) {
        try {
            handlerMethod.invoke(handlerInstance, envelope);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new HandlerExecutionException(format("Error while invoking command handler method %s with parameter:%s", handlerMethod, envelope), e);
        }
    }
}
