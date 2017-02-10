package uk.gov.justice.services.test.utils.core.matchers;

import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isCustomHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matches a Service Component Handler instance.  Uses {@link HandlerClassMatcher} to match the
 * Class of the instance.
 *
 * Example:
 *
 * <pre>
 *  {@code
 *          assertThat(new AddRecipeCommandApi(), isHandler(COMMAND_API)
 *                  .with(method("addRecipe")
 *                      .thatHandles("example.add-recipe")
 *                      .withSenderPassThrough()));
 * }
 * </pre>
 *
 * <pre>
 *  {@code
 *          assertThat(new CustomApi(), isCustomHandler("CUSTOM_API")
 *                  .with(method("addRecipe")
 *                      .thatHandles("example.add-recipe")
 *                      .withSenderPassThrough()));
 * }
 * </pre>
 */
public class HandlerMatcher extends TypeSafeDiagnosingMatcher<Object> {

    private HandlerClassMatcher handlerClassMatcher;

    public static HandlerMatcher isHandler(final String component) {
        final HandlerMatcher handlerMatcher = new HandlerMatcher();
        handlerMatcher.handlerClassMatcher = isHandlerClass(component);
        return handlerMatcher;
    }

    public static HandlerMatcher isCustomHandler(final String customComponent) {
        final HandlerMatcher handlerMatcher = new HandlerMatcher();
        handlerMatcher.handlerClassMatcher = isCustomHandlerClass(customComponent);
        return handlerMatcher;
    }

    public HandlerMatcher with(final Matcher<Class<?>> matcher) {
        this.handlerClassMatcher.with(matcher);
        return this;
    }

    @Override
    protected boolean matchesSafely(final Object handler, final Description description) {
        return handlerClassMatcher.matchesSafely(handler.getClass(), description);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Instance of ");
        description.appendDescriptionOf(handlerClassMatcher);
    }
}
