package uk.gov.justice.services.core.handler;


import static java.util.Arrays.stream;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public final class Handlers {

    private Handlers() {
    }

    /**
     * Finds all the handler methods from handler object annotated by {@link Handles}
     *
     * @param handler Handler object to be examined.
     * @return List of methods annotated with <code>Handles.class</code>.
     */
    public static List<Method> handlerMethodsFrom(final Object handler) {
        final Class<?> handlerClass = handler.getClass();
        final List<Method> handlerMethods = stream(handlerClass.getMethods())
                .filter(method -> method.isAnnotationPresent(Handles.class))
                .collect(Collectors.toList());

        if (handlerMethods.isEmpty()) {
            throw new InvalidHandlerException("Class " + handlerClass + " doesn't have any method annotated with the annotation @Handles");
        }

        return handlerMethods;
    }

}
