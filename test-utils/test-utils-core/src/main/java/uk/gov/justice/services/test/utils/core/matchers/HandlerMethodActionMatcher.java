package uk.gov.justice.services.test.utils.core.matchers;

import static uk.gov.justice.services.test.utils.core.matchers.MethodHandlesAnnotationMatcher.methodThatHandles;

import java.lang.reflect.Method;
import java.util.Optional;

import org.hamcrest.Description;

/**
 * Matcher for asserting that a method on a handler handles a certain action.
 */
public class HandlerMethodActionMatcher extends AbstractHandlerMethodMatcher {

    private final String methodName;
    private final String action;

    private HandlerMethodActionMatcher(final String methodName, final String action) {
        this.methodName = methodName;
        this.action = action;
    }

    public static HandlerMethodActionMatcher hasMethodThatHandlesAction(final String methodName, final String action) {
        return new HandlerMethodActionMatcher(methodName, action);
    }

    @Override
    protected boolean matchesSafely(final Class<?> handlerClass, final Description description) {

        final Optional<Method> method = getHandlerMethod(methodName, handlerClass, description);

        if (!method.isPresent() || !methodThatHandles(action).matches(method.get())) {
            methodThatHandles(action).describeMismatch(method.orElse(null), description);
            return false;
        }

        return true;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("a method that handles ").appendValue(action);
    }
}
