package uk.gov.justice.services.test.utils.core.matchers;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;

import java.util.Optional;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matches a Service Component Handler class.  Checks that the ServiceComponent annotation is
 * present and passes the class to an optional matcher for matching of methods.
 *
 * Following example matches a handler class that has a method named "addRecipe" that handles
 * "example.add-recipe" that is a sender pass through method.
 *
 * <pre>
 *  {@code
 *          assertThat(AddRecipeCommandApi.class, isHandlerClass(COMMAND_API)
 *                  .with(method("addRecipe")
 *                      .thatHandles("example.add-recipe")
 *                      .withSenderPassThrough()));
 * }
 * </pre>
 *
 *
 * Multiple methods can be matched by using the Matchers.allOf matcher. For example:
 *
 * <pre>
 *  {@code
 *           assertThat(AddRecipeCommandApi.class, isHandlerClass(COMMAND_API)
 *                  .with(allOf(
 *                      method("addRecipe")
 *                          .thatHandles("example.add-recipe")
 *                          .withSenderPassThrough(),
 *                      method("deleteRecipe")
 *                          .thatHandles("example.delete-recipe")
 *                          .withSenderPassThrough()))
 *           );
 * }
 * </pre>
 */
public class HandlerClassMatcher extends TypeSafeDiagnosingMatcher<Class<?>> {

    private final Component component;
    private Optional<Matcher<Class<?>>> matcher = Optional.empty();

    public HandlerClassMatcher(final Component component) {
        this.component = component;
    }

    public static HandlerClassMatcher isHandlerClass(final Component component) {
        return new HandlerClassMatcher(component);
    }

    public HandlerClassMatcher with(final Matcher<Class<?>> matcher) {
        this.matcher = Optional.of(matcher);
        return this;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Handler Class that has ");
        matcher.ifPresent(description::appendDescriptionOf);
    }

    @Override
    protected boolean matchesSafely(final Class<?> handlerClass, final Description description) {

        if (isNotServiceComponent(handlerClass)) {
            description.appendValue(handlerClass.getName())
                    .appendText(" is not annotated as a Service Component");
            return false;
        }

        if (matcher.isPresent() && !matcher.get().matches(handlerClass)) {
            matcher.get().describeMismatch(handlerClass, description);
            return false;
        }

        return true;
    }

    private boolean isNotServiceComponent(final Class<?> handlerClass) {
        return !(handlerClass.isAnnotationPresent(ServiceComponent.class)
                && handlerClass.getAnnotation(ServiceComponent.class).value().equals(component));
    }
}