package uk.gov.justice.services.core.handler;

import static java.lang.String.format;

import uk.gov.justice.services.core.handler.exception.HandlerExecutionException;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.Envelope;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Encapsulates a handler class instance and a handler method.
 *
 * Asynchronous handler methods will return a null {@link Void} whereas synchronous handler methods
 * must return an {@link Envelope}.
 */
public class HandlerMethod {

    public static final boolean SYNCHRONOUS = true;
    public static final boolean ASYNCHRONOUS = false;

    private final Object handlerInstance;
    private final Method handlerMethod;

    private final boolean isSynchronous;

    /**
     * Constructor with handler method validation.
     *
     * @param object             the instance of the handler object
     * @param method             the method on the handler object
     * @param expectedReturnType the expected return type for the method
     */
    public HandlerMethod(final Object object, final Method method, final Class<?> expectedReturnType) {

        if (object == null) {
            throw new IllegalArgumentException("Handler instance cannot be null");
        }

        if (method == null) {
            throw new IllegalArgumentException("Handler method cannot be null");
        }

        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new InvalidHandlerException(
                    format("Handles method must have exactly one parameter; found %d", parameterTypes.length));
        }
        if (parameterTypes[0] != Envelope.class) {
            throw new IllegalArgumentException(
                    format("Handler methods must take an Envelope as the argument, not a %s", parameterTypes[0]));
        }

        this.isSynchronous = !isVoid(expectedReturnType);

        if (!isSynchronous && !isVoid(method.getReturnType())) {
            throw new InvalidHandlerException("Asynchronous handler must return void");
        }
        if (isSynchronous && !isEnvelope(expectedReturnType)) {
            throw new IllegalArgumentException("Synchronous handler method must handle envelopes");
        }
        if (isSynchronous && !isEnvelope(method.getReturnType())) {
            throw new InvalidHandlerException("Synchronous handler must return an envelope");
        }

        this.handlerInstance = object;
        this.handlerMethod = method;
    }

    /**
     * Invokes the handler method passing the <code>envelope</code> to it.
     *
     * @param envelope the envelope that is passed to the handler method
     * @return the result of invoking the handler, which will either be an {@link Envelope} or a
     * null {@link Void}
     */
    @SuppressWarnings("unchecked")
    public Object execute(final Envelope envelope) {
        try {
            return handlerMethod.invoke(handlerInstance, envelope);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new HandlerExecutionException(
                    format("Error while invoking command handler method %s with parameter %s",
                            handlerMethod, envelope), ex);
        }
    }

    /**
     * Check if this handler method is synchronous.
     * @return true if the method returns a value
     */
    public boolean isSynchronous() {
        return isSynchronous;
    }

    @Override
    public String toString() {
        return format("HandlerMethod[ Class: %s method: %s]",
                handlerInstance != null ? handlerInstance.getClass().getName() : null,
                handlerMethod != null ? handlerMethod.getName() : null);
    }

    private static boolean isVoid(final Class<?> clazz) {
        return Void.TYPE.equals(clazz);
    }

    private static boolean isEnvelope(final Class<?> clazz) {
        return Envelope.class.equals(clazz);
    }
}
