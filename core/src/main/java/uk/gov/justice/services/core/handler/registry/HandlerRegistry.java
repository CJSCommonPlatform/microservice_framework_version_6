package uk.gov.justice.services.core.handler.registry;


import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.handler.HandlerInstanceAndMethod;
import uk.gov.justice.services.core.handler.registry.exception.DuplicateHandlerException;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.Envelope;

import java.lang.reflect.Method;
import java.util.HashMap;

import static java.lang.String.format;
import static java.util.Arrays.stream;

/**
 * Service for storing a map of which command handlers handle which commands.
 */
public class HandlerRegistry {

    private static final String NO_METHOD_ANNOTATION_MESSAGE = "Class %s doesn't have any method annotated with the annotation @Handles";
    private static final String MUST_HAVE_ONE_PARAMETER_MESSAGE = "Handles method must have exactly one parameter. Found %d";
    private static final String MUST_RECEIVE_ENVELOPE_MESSAGE = "Handler methods must receive Envelope as an argument.";
    private static final String DUPLICATE_METHOD_REGISTERED_MESSAGE = "Can not register %s, because a command handler method %s has already been registered for %s";

    private static final Class<Handles> ANNOTATION_CLASS = Handles.class;

    private final HashMap<String, HandlerInstanceAndMethod> handlerMapping = new HashMap<>();

    public HandlerInstanceAndMethod get(final String name) {
        return handlerMapping.get(name);
    }

    public boolean canHandle(final String name) {
        return handlerMapping.containsKey(name);
    }

    /**
     * Registers a handler instance.
     *
     * @param handlerInstance Handler instance to be registered.
     */
    public void register(final Object handlerInstance) {
        final long numberOfRegisteredMethods = registerAllAnnotatedMethodsFor(handlerInstance);

        if (numberOfRegisteredMethods == 0) {
            throw new InvalidHandlerException(format(NO_METHOD_ANNOTATION_MESSAGE, handlerInstance.getClass().getName()));
        }
    }

    /**
     * Registers all methods that are annotated as Handles and returns the number of registered methods
     *
     * @param handlerInstance Handler instance to register
     * @return number of registered methods
     */
    private long registerAllAnnotatedMethodsFor(final Object handlerInstance) {
        return stream(handlerInstance.getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(ANNOTATION_CLASS))
                .map(method -> register(handlerInstance, method))
                .count();
    }

    /**
     * Registers handler instance and method.
     *
     * @param handler       Handler instance to register.
     * @param handlerMethod Handler method to register.
     * @return true on completion
     */
    private Boolean register(final Object handler, final Method handlerMethod) {
        if (handlerMethod.getParameterTypes().length != 1) {
            throw new InvalidHandlerException(format(MUST_HAVE_ONE_PARAMETER_MESSAGE, handlerMethod.getParameterTypes().length));
        }

        final Class<?> clazz = handlerMethod.getParameterTypes()[0];
        if (clazz != Envelope.class) {
            throw new IllegalArgumentException(MUST_RECEIVE_ENVELOPE_MESSAGE);
        }

        HandlerInstanceAndMethod handlerInstanceAndMethod = new HandlerInstanceAndMethod(handler, handlerMethod);

        String name = handlerMethod.getAnnotation(ANNOTATION_CLASS).value();
        if (handlerMapping.containsKey(name)) {
            throw new DuplicateHandlerException(format(DUPLICATE_METHOD_REGISTERED_MESSAGE, handlerInstanceAndMethod.toString(), handlerMapping.get(name).toString(), name));
        }

        handlerMapping.put(handlerMethod.getAnnotation(ANNOTATION_CLASS).value(), handlerInstanceAndMethod);

        return true;
    }

}
