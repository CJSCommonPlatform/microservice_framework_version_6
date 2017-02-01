package uk.gov.justice.services.test.utils.core.matchers;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.CustomServiceComponent;
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
 * <pre>
 *  {@code
 *          assertThat(CustomApi.class, isCustomHandlerClass("CUSTOM_API")
 *                  .with(method("addRecipe")
 *                      .thatHandles("example.add-recipe")
 *                      .withSenderPassThrough()));
 * }
 * </pre>
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

    private final Optional<Component> component;
    private final Optional<String> customComponent;
    private Optional<Matcher<Class<?>>> matcher = Optional.empty();

    private HandlerClassMatcher(final Optional<Component> component, final Optional<String> customComponent) {
        this.component = component;
        this.customComponent = customComponent;
    }

    public static HandlerClassMatcher isHandlerClass(final Component component) {
        return new HandlerClassMatcher(Optional.ofNullable(component), Optional.empty());
    }

    public static HandlerClassMatcher isCustomHandlerClass(final String customComponent) {
        return new HandlerClassMatcher(Optional.empty(), Optional.ofNullable(customComponent));
    }

    public HandlerClassMatcher with(final Matcher<Class<?>> matcher) {
        this.matcher = Optional.of(matcher);
        return this;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Handler Class ");
        matcher.ifPresent(instance -> {
            description.appendText("that has ");
            description.appendDescriptionOf(instance);
        });
    }

    @Override
    protected boolean matchesSafely(final Class<?> handlerClass, final Description description) {

        if (!component.isPresent() && !customComponent.isPresent()) {
            description.appendValue(handlerClass.getName())
                    .appendText(" no annotation Component or Custom Component supplied to matcher");
            return false;
        }

        if (component.isPresent() && isNotServiceComponent(handlerClass)) {
            description.appendValue(handlerClass.getName())
                    .appendText(" is not annotated as a Service Component " + component.get().name());
            return false;
        }

        if (customComponent.isPresent() && isNotCustomServiceComponent(handlerClass)) {
            description.appendValue(handlerClass.getName())
                    .appendText(" is not annotated as a Custom Service Component " + customComponent.get());
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
                && handlerClass.getAnnotation(ServiceComponent.class).value().equals(component.get()));
    }

    private boolean isNotCustomServiceComponent(final Class<?> handlerClass) {
        return !(handlerClass.isAnnotationPresent(CustomServiceComponent.class)
                && handlerClass.getAnnotation(CustomServiceComponent.class).value().equals(customComponent.get()));
    }
}