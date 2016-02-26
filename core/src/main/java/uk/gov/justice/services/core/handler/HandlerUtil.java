package uk.gov.justice.services.core.handler;


import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class HandlerUtil {

    private HandlerUtil() {
    }

    /**
     * Finds all the handler methods from <code>handlerClass</code> annotated by {@link Handles}
     *
     * @param handlerClass Handler class to be examined.
     * @return List of methods annotated with <code>annotationClass</code>.
     */
    public static List<Method> findHandlerMethods(final Class<?> handlerClass, final Class<? extends Annotation> annotationClass) {
        final Method[] methods = handlerClass.getMethods();
        final List<Method> handlerMethods = new ArrayList<>();

        for (Method method : methods) {
            if (method.isAnnotationPresent(annotationClass)) {
                handlerMethods.add(method);
            }
        }

        if (handlerMethods.isEmpty()) {
            throw new InvalidHandlerException("Class " + handlerClass + " doesn't have any method annotated with the annotation @Handles");
        }

        return handlerMethods;
    }

}
