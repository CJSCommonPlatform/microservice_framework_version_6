package uk.gov.justice.services.test.utils.core.matchers;

import uk.gov.justice.services.core.annotation.Handles;

import java.lang.reflect.Method;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matches a Method with the @Handles annotation with the specified value.
 *
 * The following matches the exampleMethod for the @Handles annotation with "example.event" value
 * set:
 *
 * <pre>
 *  {@code
 *      assertThat(Example.class.getMethod("exampleMethod"), methodThatHandles("example.event"));
 * }
 * </pre>
 */
public class MethodHandlesAnnotationMatcher extends TypeSafeDiagnosingMatcher<Method> {

    private final String action;

    public MethodHandlesAnnotationMatcher(final String action) {
        this.action = action;
    }

    public static MethodHandlesAnnotationMatcher methodThatHandles(final String action) {
        return new MethodHandlesAnnotationMatcher(action);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Does method handle: ").appendValue(action);
    }

    @Override
    protected boolean matchesSafely(final Method method, final Description description) {

        if (method.isAnnotationPresent(Handles.class)) {
            final Handles annotation = method.getAnnotation(Handles.class);
            if (annotation.value().equals(action)) {
                return true;
            }
        }

        description.appendText("Method: ")
                .appendValue(method.getName())
                .appendText(" does not handle: ")
                .appendValue(action);

        return false;
    }
}
