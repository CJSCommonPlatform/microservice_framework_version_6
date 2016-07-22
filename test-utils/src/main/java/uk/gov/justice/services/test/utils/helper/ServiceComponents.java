package uk.gov.justice.services.test.utils.helper;

import static java.lang.String.format;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ServiceComponents {

    private ServiceComponents() {
    }

    /**
     * Verify all the pass through handler methods of the specified handler class. This will use
     * reflection to find all methods that have a Handles annotation and verify each method.
     * Verifies there is a ServiceComponent annotation, each method has a @Handles annotation, the
     * Sender field is present, and the sender.send method is called with the original command
     * passed to the handler method.
     *
     * @param handlerClass the handler class to verify
     */
    public static void verifyPassThroughCommandHandlerMethod(final Class<?> handlerClass) throws Exception {
        verifyPassThroughCommandHandlerMethod(handlerClass,
                Stream.of(handlerClass.getMethods())
                        .filter(ServiceComponents::hasHandlesAnnotation)
                        .collect(Collectors.toList()));
    }

    /**
     * Verify a single or multiple named methods of a handler class.  Verifies there is a
     * ServiceComponent annotation, each method has a Handles annotation, the Sender field is
     * present, and the sender.send method is called with the original command passed to the
     * handler method.
     *
     * @param handlerClass the handler class to verify
     * @param methodNames  the method names to verify
     */
    public static void verifyPassThroughCommandHandlerMethod(final Class<?> handlerClass, final String... methodNames) throws Exception {
        List<Method> methods = new ArrayList<>();

        for (String methodName : methodNames) {
            methods.add(handlerClass.getMethod(methodName, JsonEnvelope.class));
        }

        verifyPassThroughCommandHandlerMethod(handlerClass, methods);
    }

    /**
     * Verify a list of method names of a handler.  Verifies there is a ServiceComponent
     * annotation, each method has a Handles annotation, the Sender field is present, and the
     * sender.send method is called with the original command passed to the handler method.
     *
     * @param handlerClass the handler class to verify
     * @param methodNames  the method names to verify
     */
    public static void verifyPassThroughCommandHandlerMethod(final Class<?> handlerClass, final List<Method> methodNames) throws Exception {

        if (isNotServiceComponent(handlerClass)) {
            throw new AssertionError(format("No @ServiceComponent annotation present on Class %s", handlerClass.getSimpleName()));
        }

        if (methodNames.isEmpty()) {
            throw new AssertionError(format("No @Handles annotation present, or no Handler methods for class %s", handlerClass.getSimpleName()));
        }

        for (final Method method : methodNames) {
            final Sender sender = mock(Sender.class);
            final JsonEnvelope command = mock(JsonEnvelope.class);

            final Object handlerInstance = handlerClass.newInstance();

            if (hasNoHandlesAnnotation(method)) {
                throw new AssertionError(format("No @Handles annotation present on Method %s", method.getName()));
            }

            final Field senderField = findSenderField(handlerClass);
            senderField.setAccessible(true);
            senderField.set(handlerInstance, sender);

            method.invoke(handlerInstance, command);

            verify(sender).send(command);
        }
    }

    private static boolean isNotServiceComponent(final Class<?> handlerClass) {
        return !Stream.of(handlerClass.getAnnotations())
                .filter(annotation -> annotation.annotationType().equals(ServiceComponent.class))
                .findFirst()
                .isPresent();
    }

    private static boolean hasNoHandlesAnnotation(final Method handlerMethod) {
        return !hasHandlesAnnotation(handlerMethod);
    }

    private static boolean hasHandlesAnnotation(final Method handlerMethod) {
        return Stream.of(handlerMethod.getDeclaredAnnotations())
                .filter(annotation -> annotation.annotationType().equals(Handles.class))
                .findFirst()
                .isPresent();
    }

    private static Field findSenderField(final Class<?> handlerClass) {
        return Stream.of(handlerClass.getDeclaredFields())
                .filter(field -> field.getType().equals(Sender.class))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No field of class type Sender found in handler class"));
    }
}
