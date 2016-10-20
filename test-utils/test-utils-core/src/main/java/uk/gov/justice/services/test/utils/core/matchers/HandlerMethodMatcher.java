package uk.gov.justice.services.test.utils.core.matchers;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.PassThroughType.REQUESTER;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.PassThroughType.SENDER;
import static uk.gov.justice.services.test.utils.core.matchers.MethodHandlesAnnotationMatcher.methodThatHandles;

import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.hamcrest.Description;
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

    private static final int ONCE = 1;
    private String methodName;
    private Optional<String> action = Optional.empty();
    private Optional<PassThroughType> passThroughType = Optional.empty();

    public HandlerMethodMatcher(final String methodName) {
        this.methodName = methodName;
    }

    public static HandlerMethodMatcher method(final String methodName) {
        return new HandlerMethodMatcher(methodName);
    }

    public HandlerMethodMatcher thatHandles(final String action) {
        this.action = Optional.of(action);
        return this;
    }

    public HandlerMethodMatcher withSenderPassThrough() {
        this.passThroughType = Optional.of(SENDER);
        return this;
    }

    public HandlerMethodMatcher withRequesterPassThrough() {
        this.passThroughType = Optional.of(REQUESTER);
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
                    .appendValue(methodName);
            return false;
        }

        if (action.isPresent() && !methodThatHandles(action.get()).matches(method)) {
            methodThatHandles(action.get()).describeMismatch(method, description);
            return false;
        }

        if (passThroughType.isPresent()) {
            try {
                switch (passThroughType.get()) {
                    case SENDER:
                        return isSenderPassthrough(handlerClass, method);
                    case REQUESTER:
                        return isRequesterPassthrough(handlerClass, method);
                }
            } catch (final Exception ex) {
                description.appendText("Method ")
                        .appendValue(methodName)
                        .appendText(" of class ")
                        .appendValue(handlerClass)
                        .appendText(" is not a ")
                        .appendText(passThroughType.get().toString())
                        .appendText(" pass-through method");
                return false;
            }
        }

        return true;
    }

    private boolean isSenderPassthrough(final Class<?> handlerClass, final Method method) throws Exception {
        final Sender sender = mock(Sender.class, format("%s.sender.send", method.getName()));
        final JsonEnvelope command = mock(JsonEnvelope.class);
        final Object handlerInstance = handlerClass.newInstance();

        final Field senderField = findField(handlerClass, Sender.class);
        senderField.setAccessible(true);
        senderField.set(handlerInstance, sender);

        method.invoke(handlerInstance, command);
        verify(sender, times(ONCE)).send(command);

        return true;
    }

    private boolean isRequesterPassthrough(final Class<?> handlerClass, final Method method) throws Exception {
        final Requester requester = mock(Requester.class, format("%s.requester.request", method.getName()));
        final JsonEnvelope query = mock(JsonEnvelope.class);
        final JsonEnvelope response = mock(JsonEnvelope.class);
        final Object handlerInstance = handlerClass.newInstance();

        final Field requesterField = findField(handlerClass, Requester.class);
        requesterField.setAccessible(true);
        requesterField.set(handlerInstance, requester);

        when(requester.request(query)).thenReturn(response);

        assertThat(method.invoke(handlerInstance, query), is(response));
        verify(requester, times(ONCE)).request(query);

        return true;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("a command pass through method of ").appendValue(methodName);
        action.ifPresent(value -> description.appendText(" that handles ").appendValue(value));
        passThroughType.ifPresent(type ->
                description.appendText(" that is a ")
                        .appendText(type.toString())
                        .appendText(" pass-through method"));
    }

    private Field findField(final Class<?> handlerClass, final Class<?> fieldClass) {
        return Stream.of(handlerClass.getDeclaredFields())
                .filter(field -> field.getType().equals(fieldClass))
                .findFirst()
                .orElseThrow(() -> new AssertionError(format("No field of class type %s found in handler class", fieldClass.getSimpleName())));
    }

    enum PassThroughType {

        SENDER("sender"), REQUESTER("requester");

        private final String description;

        PassThroughType(final String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
