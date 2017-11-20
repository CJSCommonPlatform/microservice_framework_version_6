package uk.gov.justice.services.core.handler;

import static java.lang.Class.forName;
import static java.lang.String.format;
import static uk.gov.justice.services.messaging.logging.LoggerUtils.trace;

import uk.gov.justice.services.core.annotation.Direct;
import uk.gov.justice.services.core.handler.exception.HandlerExecutionException;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import javax.json.JsonValue;

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
    private final Class<?> payloadType;

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

        final Class<?> methodArgType = parameterTypes[0];

        if (!Envelope.class.isAssignableFrom(methodArgType)) {
            throw new IllegalArgumentException(
                    format("Handler methods must take an JsonEnvelope or Envelope<T> as the argument, not a %s", methodArgType));
        }

        if(!methodArgType.equals(JsonEnvelope.class)) {
            final Type[] genericParameterTypes = method.getGenericParameterTypes();
            final Type[] parameters = ((ParameterizedType)genericParameterTypes[0]).getActualTypeArguments();
            try {
                payloadType = forName(parameters[0].getTypeName());
            } catch (ClassNotFoundException e) {
                throw new HandlerCreationException(e);
            }
        } else {
            payloadType = JsonValue.class;
        }

        this.isSynchronous = !isVoid(expectedReturnType);

        if (!isSynchronous && !isVoid(method.getReturnType())) {
            throw new InvalidHandlerException("Asynchronous handler must return void");
        }
        if (isSynchronous && !Envelope.class.isAssignableFrom(expectedReturnType)) {
            throw new IllegalArgumentException("Synchronous handler method must handle envelopes");
        }
        if (isSynchronous && !Envelope.class.isAssignableFrom(method.getReturnType())) {
            throw new InvalidHandlerException("Synchronous handler must return an envelope");
        }

        this.handlerInstance = object;
        this.handlerMethod = method;
    }

    private static boolean isVoid(final Class<?> clazz) {
        return Void.TYPE.equals(clazz);
    }

    /**
     * Invokes the handler method passing the <code>envelope</code> to it.
     *
     * @param envelope the envelope that is passed to the handler method
     * @return the result of invoking the handler, which will either be an {@link JsonEnvelope} or a
     * null {@link Void}
     */
    @SuppressWarnings("unchecked")
    public <T> Envelope<T> execute(final Envelope<?> envelope) {
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

            return (Envelope<T>) obj;

        } catch (Exception ex) {
            if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            } else {
                throw handlerExecutionExceptionOf(envelope, ex.getCause());
            }
        }
    }

    private HandlerExecutionException handlerExecutionExceptionOf(final Envelope envelope, final Throwable cause) {
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

    public boolean isDirect() {
        return handlerInstance.getClass().isAnnotationPresent(Direct.class);
    }

    public Class<?> getPayloadType() {
        return payloadType;
    }
}
