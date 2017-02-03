package uk.gov.justice.services.test.utils.core.matchers;

import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodActionMatcher.hasMethodThatHandlesAction;
import static uk.gov.justice.services.test.utils.core.matchers.RequesterPassThroughMatcher.hasRequesterPassThroughMethod;
import static uk.gov.justice.services.test.utils.core.matchers.SenderPassThroughMatcher.hasSenderPassThroughMethod;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matches a Class that has a Handler method of a specified name that has a @Handles annotation with
 * a specified value.
 *
 * The following matches a Class with a method named "example" that handles a "example.event":
 *
 * <pre>
 *  {@code
 *          assertThat(AddRecipeCommandApi.class, method("example").thatHandles("example.event"));
 * }
 * </pre>
 */
public class HandlerMethodMatcher extends TypeSafeDiagnosingMatcher<Class<?>> {

    private String methodName;

    private List<Matcher<Class<?>>> matchers = new ArrayList<>();

    public HandlerMethodMatcher(final String methodName) {
        this.methodName = methodName;
    }

    public static HandlerMethodMatcher method(final String methodName) {
        return new HandlerMethodMatcher(methodName);
    }

    public HandlerMethodMatcher thatHandles(final String action) {
        matchers.add(hasMethodThatHandlesAction(methodName, action));
        return this;
    }

    public HandlerMethodMatcher withSenderPassThrough() {
        matchers.add(hasSenderPassThroughMethod(methodName));
        return this;
    }

    public HandlerMethodMatcher withRequesterPassThrough() {
        matchers.add(hasRequesterPassThroughMethod(methodName));
        return this;
    }

    @Override
    protected boolean matchesSafely(final Class<?> handlerClass, final Description description) {

        final Method method;
        try {
            method = handlerClass.getMethod(methodName, JsonEnvelope.class);
        } catch (final Exception ex) {
            description
                    .appendText("Class ")
                    .appendValue(handlerClass)
                    .appendText("has no method ")
                    .appendValue(methodName)
                    .appendText(" with an argument of type JsonEnvelope ");
            return false;
        }

        for (final Matcher matcher : matchers) {
            if (!matcher.matches(handlerClass)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("a method of ").appendValue(methodName);
        for (Matcher matcher : matchers) {
            matcher.describeTo(description);
        }
    }
}
