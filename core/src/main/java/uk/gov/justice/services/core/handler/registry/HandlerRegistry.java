package uk.gov.justice.services.core.handler.registry;


import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.handler.HandlerInstanceAndMethod;
import uk.gov.justice.services.core.handler.HandlerUtil;
import uk.gov.justice.services.core.handler.registry.exception.DuplicateHandlerException;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.Envelope;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/**
 * Service for storing a map of which command handlers handle which commands.
 */
public class HandlerRegistry {

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
        final List<Method> handlerMethods = HandlerUtil.findHandlerMethods(handlerInstance.getClass(), Handles.class);

        for (Method handlerMethod : handlerMethods) {
            register(handlerInstance, handlerMethod);
        }
    }

    /**
     * Registers handler instance and method.
     *
     * @param handler       Handler instance to register.
     * @param handlerMethod Handler method to register.
     */
    private void register(final Object handler, final Method handlerMethod) {
        if (handlerMethod.getParameterTypes().length != 1) {
            throw new InvalidHandlerException("Handles method must have exactly one parameter. Found " + handlerMethod.getParameterTypes().length);
        }

        final Class<?> clazz = handlerMethod.getParameterTypes()[0];
        if (clazz != Envelope.class) {
            throw new IllegalArgumentException("Handler methods must receive Envelope as an argument.");
        }

        HandlerInstanceAndMethod handlerInstanceAndMethod = new HandlerInstanceAndMethod(handler, handlerMethod);

        String name = handlerMethod.getAnnotation(Handles.class).value();
        if (handlerMapping.containsKey(name)) {
            throw new DuplicateHandlerException("Can't register " + handlerInstanceAndMethod + ", because a command handler method "
                    + handlerMapping.get(clazz) + " has already been registered for " + name);
        }

        handlerMapping.put(handlerMethod.getAnnotation(Handles.class).value(), handlerInstanceAndMethod);
    }

}
