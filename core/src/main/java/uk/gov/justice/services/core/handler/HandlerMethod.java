package uk.gov.justice.services.core.handler;

import static java.lang.String.format;
import static uk.gov.justice.services.messaging.logging.LoggerUtils.trace;

import uk.gov.justice.services.core.handler.exception.HandlerExecutionException;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates a handler class instance and a handler method.
 *
 * Asynchronous handler methods will return a null {@link Void} whereas synchronous handler methods
 * must return an {@link JsonEnvelope}.
 */
public class HandlerMethod {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerMethod.class);

    private final Object handlerInstance;
    private final Method handlerMethod;

    private final boolean isSynchronous;

    /**
     * Constructor with handler method validator.
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
        if (parameterTypes[0] != JsonEnvelope.class) {
            throw new IllegalArgumentException(
                    format("Handler methods must take an JsonEnvelope as the argument, not a %s", parameterTypes[0]));
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

    private static boolean isVoid(final Class<?> clazz) {
        return Void.TYPE.equals(clazz);
    }

    private static boolean isEnvelope(final Class<?> clazz) {
        return JsonEnvelope.class.equals(clazz);
    }

    /**
     * Invokes the handler method passing the <code>envelope</code> to it.
     *
     * @param envelope the envelope that is passed to the handler method
     * @return the result of invoking the handler, which will either be an {@link JsonEnvelope} or a
     * null {@link Void}
     */
    @SuppressWarnings("unchecked")
    public Object execute(final JsonEnvelope envelope) {
        trace(LOGGER, () -> format("Dispatching to handler %s.%s : %s",
                handlerInstance.getClass().toString(),
                handlerMethod.getName(),
                envelope));
        try {
            final Object obj = handlerMethod.invoke(handlerInstance, envelope);
            trace(LOGGER, () -> {

                final Optional<Object> response = Optional.ofNullable(obj);

                if (response.isPresent() && response.get() instanceof JsonEnvelope) {
                    return format("Response received from handler %s.%s : %s",
                            handlerInstance.getClass().toString(),
                            handlerMethod.getName(),
                            response.get());
                }

                return format("Response from handler %s.%s with id %s was void",
                        handlerInstance.getClass().toString(),
                        handlerMethod.getName(),
                        envelope.metadata().id().toString());
            });

            return obj;

        } catch (IllegalAccessException ex) {
            throw handlerExecutionExceptionOf(envelope, ex.getCause());
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            } else {
                throw handlerExecutionExceptionOf(envelope, ex.getCause());
            }
        }
    }

    private HandlerExecutionException handlerExecutionExceptionOf(final JsonEnvelope envelope, final Throwable cause) {
        return new HandlerExecutionException(
                format("Error while invoking handler method %s with parameter %s",
                        handlerMethod, envelope), cause);
    }

    @Override
    public String toString() {
        return format("HandlerMethod[ Class: %s method: %s]",
                handlerInstance != null ? handlerInstance.getClass().getName() : null,
                handlerMethod != null ? handlerMethod.getName() : null);
    }
}
