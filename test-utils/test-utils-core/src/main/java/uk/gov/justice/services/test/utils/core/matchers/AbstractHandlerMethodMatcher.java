package uk.gov.justice.services.test.utils.core.matchers;

import static java.lang.String.format;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.mock.SkipJsonValidationListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Base class with helper methods for handler method matchers.
 */
public abstract class AbstractHandlerMethodMatcher extends TypeSafeDiagnosingMatcher<Class<?>> {

    protected static final int ONCE = 1;
    protected static final SkipJsonValidationListener SKIP_JSON_VALIDATION_LISTENER = new SkipJsonValidationListener();

    protected Optional<Method> getHandlerMethod(final String methodName, final Class<?> handlerClass, final Description description) {
        try {
            return Optional.of(handlerClass.getMethod(methodName, JsonEnvelope.class));
        } catch (final Exception ex) {
            description
                    .appendText("Class ")
                    .appendValue(handlerClass)
                    .appendText("has no method ")
                    .appendValue(methodName)
                    .appendText(" with an argument of type JsonEnvelope ");
            return Optional.empty();
        }
    }

    protected Field findField(final Class<?> handlerClass, final Class<?> fieldClass) {
        return Stream.of(handlerClass.getDeclaredFields())
                .filter(field -> field.getType().equals(fieldClass))
                .findFirst()
                .orElseThrow(() -> new AssertionError(format("No field of class type %s found in handler class", fieldClass.getSimpleName())));
    }
}
