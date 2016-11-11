package uk.gov.justice.services.test.utils.core.helper;

import static java.lang.String.format;
import static org.mockito.Answers.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher;
import uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher;
import uk.gov.justice.services.test.utils.core.mock.SkipJsonValidationListener;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Use new matcher classes for matching Handlers {@link HandlerMatcher} and {@link
 * HandlerClassMatcher}
 */
@Deprecated
public final class ServiceComponents {

    private static final SkipJsonValidationListener SKIP_JSON_VALIDATION_LISTENER = new SkipJsonValidationListener();

    private ServiceComponents() {
    }

    /**
     * Verify all the pass through handler methods of the specified Command handler class. This will
     * use reflection to find all methods that have a Handles annotation and verify each method.
     * Verifies there is a ServiceComponent annotation, each method has a @Handles annotation, the
     * Sender field is present, and the sender.send method is called with the original command
     * passed to the handler method.
     *
     * @param handlerClass the handler class to verify
     * @throws Exception if non pass through method or an error occurs
     */
    public static void verifyPassThroughCommandHandlerMethod(final Class<?> handlerClass) throws Exception {
        verifyPassThroughCommandHandlerMethod(handlerClass,
                Stream.of(handlerClass.getMethods())
                        .filter(ServiceComponents::hasHandlesAnnotation)
                        .collect(Collectors.toList()));
    }

    /**
     * Verify a single or multiple named methods of a Command handler class.  Verifies there is a
     * ServiceComponent annotation, each method has a Handles annotation, the Sender field is
     * present, and the sender.send method is called with the original command passed to the
     * handler method.
     *
     * @param handlerClass the handler class to verify
     * @param methodNames  the method names to verify
     * @throws Exception if non pass through method or an error occurs
     */
    public static void verifyPassThroughCommandHandlerMethod(final Class<?> handlerClass, final String... methodNames) throws Exception {
        List<Method> methods = new ArrayList<>();

        for (String methodName : methodNames) {
            methods.add(handlerClass.getMethod(methodName, JsonEnvelope.class));
        }

        verifyPassThroughCommandHandlerMethod(handlerClass, methods);
    }

    /**
     * Verify a list of method names of a Command handler.  Verifies there is a ServiceComponent
     * annotation, each method has a Handles annotation, the Sender field is present, and the
     * sender.send method is called with the original command passed to the handler method.
     *
     * @param handlerClass the handler class to verify
     * @param methods      the method names to verify
     * @throws Exception if non pass through method or an error occurs
     */
    public static void verifyPassThroughCommandHandlerMethod(final Class<?> handlerClass, final List<Method> methods) throws Exception {

        assertIsServiceComponent(handlerClass);
        assertHandlerHasMethods(handlerClass, methods);

        for (final Method method : methods) {
            assertMethodHasHandlesAnnotation(method);

            final Sender sender = mock(Sender.class, withSettings()
                    .name(format("%s.sender.send", method.getName()))
                    .invocationListeners(SKIP_JSON_VALIDATION_LISTENER)
                    .defaultAnswer(RETURNS_DEFAULTS.get()));

            final JsonEnvelope command = envelope().with(metadataWithDefaults()).build();
            final Object handlerInstance = handlerClass.newInstance();

            final Field senderField = findField(handlerClass, Sender.class);
            senderField.setAccessible(true);
            senderField.set(handlerInstance, sender);

            method.invoke(handlerInstance, command);

            verify(sender).send(command);
        }
    }

    /**
     * Verify all the pass through handler methods of the specified Query handler class. This will
     * use reflection to find all methods that have a Handles annotation and verify each method.
     * Verifies there is a ServiceComponent annotation, each method has a @Handles annotation, the
     * Requester field is present, and the requester.request method is called with the original
     * query passed to the handler method.
     *
     * @param handlerClass the handler class to verify
     * @throws Exception if non pass through method or an error occurs
     */
    public static void verifyPassThroughQueryHandlerMethod(final Class<?> handlerClass) throws Exception {
        verifyPassThroughQueryHandlerMethod(handlerClass,
                Stream.of(handlerClass.getMethods())
                        .filter(ServiceComponents::hasHandlesAnnotation)
                        .collect(Collectors.toList()));
    }

    /**
     * Verify a single or multiple named methods of a Query handler class.  Verifies there is a
     * ServiceComponent annotation, each method has a Handles annotation, the Requester field is
     * present, and the requester.request method is called with the original command passed to the
     * handler method.
     *
     * @param handlerClass the handler class to verify
     * @param methodNames  the method names to verify
     * @throws Exception if non pass through method or an error occurs
     */
    public static void verifyPassThroughQueryHandlerMethod(final Class<?> handlerClass, final String... methodNames) throws Exception {
        List<Method> methods = new ArrayList<>();

        for (String methodName : methodNames) {
            methods.add(handlerClass.getMethod(methodName, JsonEnvelope.class));
        }

        verifyPassThroughQueryHandlerMethod(handlerClass, methods);
    }

    /**
     * Verify a list of method names of a Query handler.  Verifies there is a ServiceComponent
     * annotation, each method has a Handles annotation, the Requester field is present, and the
     * requester.request method is called with the original command passed to the handler method.
     *
     * @param handlerClass the handler class to verify
     * @param methods      the method names to verify
     * @throws Exception if non pass through method or an error occurs
     */
    public static void verifyPassThroughQueryHandlerMethod(final Class<?> handlerClass, final List<Method> methods) throws Exception {

        assertIsServiceComponent(handlerClass);
        assertHandlerHasMethods(handlerClass, methods);

        for (final Method method : methods) {
            assertMethodHasHandlesAnnotation(method);


            final Requester requester = mock(Requester.class,
                    withSettings()
                            .name(format("%s.requester", method.getName()))
                            .invocationListeners(SKIP_JSON_VALIDATION_LISTENER)
                            .defaultAnswer(RETURNS_DEFAULTS.get()));
            final JsonEnvelope query = envelope().with(metadataWithDefaults()).build();
            final JsonEnvelope response = envelope().with(metadataWithDefaults()).build();
            final Object handlerInstance = handlerClass.newInstance();

            final Field requesterField = findField(handlerClass, Requester.class);
            requesterField.setAccessible(true);
            requesterField.set(handlerInstance, requester);

            when(requester.request(query)).thenReturn(response);

            JsonEnvelope actualResponse = (JsonEnvelope) method.invoke(handlerInstance, query);

            if (actualResponse == null || !actualResponse.equals(response)) {
                throw new AssertionError(format("JsonEnvelope response does not match expected response in method %s.", method.getName()));
            }

            verify(requester).request(query);
        }
    }

    private static void assertIsServiceComponent(final Class<?> handlerClass) {
        if (isNotServiceComponent(handlerClass)) {
            throw new AssertionError(format("No @ServiceComponent annotation present on Class %s", handlerClass.getSimpleName()));
        }
    }

    private static void assertMethodHasHandlesAnnotation(final Method method) {
        if (hasNoHandlesAnnotation(method)) {
            throw new AssertionError(format("No @Handles annotation present on Method %s", method.getName()));
        }
    }

    private static void assertHandlerHasMethods(final Class<?> handlerClass, final List<Method> methods) {
        if (methods.isEmpty()) {
            throw new AssertionError(format("No @Handles annotation present, or no Handler methods for class %s", handlerClass.getSimpleName()));
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
        return handlesAnnotationOf(handlerMethod).isPresent();
    }

    private static Optional<Annotation> handlesAnnotationOf(final Method handlerMethod) {
        return Stream.of(handlerMethod.getDeclaredAnnotations())
                .filter(annotation -> annotation.annotationType().equals(Handles.class))
                .findFirst();
    }

    private static Field findField(final Class<?> handlerClass, final Class<?> fieldClass) {
        return Stream.of(handlerClass.getDeclaredFields())
                .filter(field -> field.getType().equals(fieldClass))
                .findFirst()
                .orElseThrow(() -> new AssertionError(format("No field of class type %s found in handler class", fieldClass.getSimpleName())));
    }
}
