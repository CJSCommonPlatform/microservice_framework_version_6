package uk.gov.justice.services.core.handler;

import static java.lang.String.format;

import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.Envelope;

import java.lang.reflect.Method;

/**
 * Base class for synchronous and asynchronous handler methods.
 */
public abstract class HandlerMethod {

    protected final Object handlerInstance;
    protected final Method handlerMethod;

    /**
     * Constructor with common handler method validation.
     *
     * @param handlerInstance the instance of the handler object
     * @param handlerMethod   the method on the handler object
     */
    HandlerMethod(final Object handlerInstance, final Method handlerMethod) {

        if (handlerInstance == null) {
            throw new IllegalArgumentException("Handler instance cannot be null");
        }

        if (handlerMethod == null) {
            throw new IllegalArgumentException("Handler method cannot be null");
        }

        if (handlerMethod.getParameterTypes().length != 1) {
            throw new InvalidHandlerException("Handles method must have exactly one parameter. Found " + handlerMethod.getParameterTypes().length);
        }

        final Class<?> clazz = handlerMethod.getParameterTypes()[0];
        if (clazz != Envelope.class) {
            throw new IllegalArgumentException("Handler methods must receive Envelope as an argument.");
        }

        this.handlerInstance = handlerInstance;
        this.handlerMethod = handlerMethod;
    }

    @Override
    public String toString() {
        return format("HandlerInstanceAndMethod[ Class:%s method:%s]", handlerInstance != null ? handlerInstance.getClass().getName() : null,
                handlerMethod != null ? handlerMethod.getName() : null);
    }

}
