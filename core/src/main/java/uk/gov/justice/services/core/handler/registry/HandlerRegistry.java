package uk.gov.justice.services.core.handler.registry;

import static uk.gov.justice.services.core.handler.HandlerUtil.findHandlerMethods;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.handler.AsynchronousHandlerMethod;
import uk.gov.justice.services.core.handler.HandlerMethod;
import uk.gov.justice.services.core.handler.SynchronousHandlerMethod;
import uk.gov.justice.services.core.handler.registry.exception.DuplicateHandlerException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Service for storing a map of which command handlers handle which commands.
 */
public class HandlerRegistry {

    private final Map<String, AsynchronousHandlerMethod> asynchronousMethods = new HashMap<>();
    private final Map<String, SynchronousHandlerMethod> synchronousMethods = new HashMap<>();

    public AsynchronousHandlerMethod getAsynchronous(final String name) {
        return asynchronousMethods.get(name);
    }

    public SynchronousHandlerMethod getSynchronous(final String name) {
        return synchronousMethods.get(name);
    }

    public boolean canHandleAsynchronous(final String name) {
        return asynchronousMethods.containsKey(name);
    }

    public boolean canHandleSynchronous(final String name) {
        return synchronousMethods.containsKey(name);
    }

    /**
     * Registers a handler instance.
     *
     * @param handlerInstance Handler instance to be registered.
     */
    public void register(final Object handlerInstance) {
        final List<Method> handlerMethods = findHandlerMethods(handlerInstance.getClass(), Handles.class);

        for (Method handlerMethod : handlerMethods) {
            register(handlerInstance, handlerMethod);
        }
    }

    /**
     * Registers handler instance and method.
     *
     * @param handler handler instance to register.
     * @param method handler method to register.
     */
    private void register(final Object handler, final Method method) {

        if (Void.TYPE.equals(method.getReturnType())) {
            put(handler, method, AsynchronousHandlerMethod::new, asynchronousMethods);
        } else {
            put(handler, method, SynchronousHandlerMethod::new, synchronousMethods);
        }
    }

    private <T extends HandlerMethod> void put(final Object handler,
                                               final Method method,
                                               final BiFunction<Object, Method, T> constructor,
                                               final Map<String, T> map) {

        T handlerMethod = constructor.apply(handler, method);
        String name = method.getAnnotation(Handles.class).value();
        if (map.containsKey(name)) {
            throw new DuplicateHandlerException("Can't register " + handlerMethod + ", because a command handler method "
                    + map.get(name) + " has already been registered for " + name);
        }

        map.put(name, handlerMethod);
    }
}
