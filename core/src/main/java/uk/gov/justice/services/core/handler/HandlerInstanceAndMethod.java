package uk.gov.justice.services.core.handler;

import uk.gov.justice.services.core.handler.exception.HandlerExecutionException;
import uk.gov.justice.services.messaging.Envelope;

import java.lang.reflect.Method;

/**
 * Wrapper class for executing a specific handling method on a command handler instance.
 */
public class HandlerInstanceAndMethod {

    private final Object handlerInstance;
    private final Method handlerMethod;

    public HandlerInstanceAndMethod(final Object handlerInstance, final Method handlerMethod) {
        this.handlerInstance = handlerInstance;
        this.handlerMethod = handlerMethod;
    }

    /**
     * Invokes the handler method passing the <code>envelope</code> to it.
     *
     * @param envelope Envelope that is passed to the handler method.
     */
    public void execute(final Envelope envelope) {
        Method method = null;

        try {
            method = handlerInstance.getClass().getMethod(handlerMethod.getName(), handlerMethod.getParameterTypes());
            method.invoke(handlerInstance, envelope);
        } catch (Exception e) {
            throw new HandlerExecutionException(String.format("Error while invoking command handler method %s with parameter:%s", method, envelope), e);
        }
    }

    @Override
    public String toString() {
        return String.format("HandlerInstanceAndMethod[ Class:%s method:%s]", (handlerInstance != null ? handlerInstance.getClass().getName() : null),
                (handlerMethod != null ? handlerMethod.getName() : null));
    }

}
