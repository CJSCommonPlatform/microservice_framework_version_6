package uk.gov.justice.services.test.utils.core.matchers;

import static java.lang.Class.forName;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.mockito.Answers.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.PassThroughType.REQUESTER;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.PassThroughType.SENDER;
import static uk.gov.justice.services.test.utils.core.matchers.MethodHandlesAnnotationMatcher.methodThatHandles;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.mock.SkipJsonValidationListener;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.el.MethodNotFoundException;

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
    private static final SkipJsonValidationListener SKIP_JSON_VALIDATION_LISTENER = new SkipJsonValidationListener();
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

        Method method = null;
        try {
            final List<Method> methods = Arrays.asList(handlerClass.getDeclaredMethods());
            for (final Method method1 : methods) {
                if (method1.getName() == methodName && validType(method1.getParameterTypes()[0])) {
                    method = method1;
                }
            }

            if (method == null) {
                throw new MethodNotFoundException(format("Method %s is not matched for class %s", methodName, handlerClass));
            }
        } catch (final Exception ex) {
            description
                    .appendText("Class ")
                    .appendValue(handlerClass)
                    .appendText("has no method ")
                    .appendValue(methodName)
                    .appendText(" with an argument of type JsonEnvelope ");
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
                        return isSenderPassThrough(handlerClass, method);
                    case REQUESTER:
                        return isRequesterPassThrough(handlerClass, method);
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

    private boolean validType(final Class<?> aClass) {
        return Envelope.class.isAssignableFrom(aClass);
    }

    private boolean isSenderPassThrough(final Class<?> handlerClass, final Method method) throws Exception {
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

        return true;
    }

    private boolean isRequesterPassThrough(final Class<?> handlerClass, final Method method) throws Exception {
        final Requester requester = mock(Requester.class,
                withSettings()
                        .name(format("%s.requester.request", method.getName()))
                        .invocationListeners(SKIP_JSON_VALIDATION_LISTENER)
                        .defaultAnswer(RETURNS_DEFAULTS.get()));
        final Envelope query = envelope().with(metadataWithDefaults()).build();

        final Object handlerInstance = handlerClass.newInstance();

        final Field requesterField = findField(handlerClass, Requester.class);
        requesterField.setAccessible(true);
        requesterField.set(handlerInstance, requester);

        return method.getReturnType().getName().contains("JsonEnvelope")?
                verifyRequesterMethodJsonEnvelopeCall(method, requester, query, handlerInstance):
                verifyRequesterMethodPojoEnvelopeCall(method, requester, query, handlerInstance, method.getGenericReturnType().getTypeName());
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


    private boolean verifyRequesterMethodJsonEnvelopeCall(final Method method,
                                                          final Requester requester,
                                                          final Envelope query,
                                                          final Object handlerInstance) throws IllegalAccessException, InvocationTargetException {
       final JsonEnvelope jsonEnvelopeResponse = envelope().with(metadataWithDefaults()).build();
       when(requester.request(query)).thenReturn(jsonEnvelopeResponse);

       assertThat(method.invoke(handlerInstance, query), is(jsonEnvelopeResponse));
       verify(requester).request(query);
       return true;
    }

    private boolean verifyRequesterMethodPojoEnvelopeCall(final Method method,
                                                          final Requester requester,
                                                          final Envelope query,
                                                          final Object handlerInstance,
                                                          final String fullClassName) throws IllegalAccessException, InvocationTargetException, ClassNotFoundException {
       final Envelope response = envelope().with(metadataWithDefaults()).build();
       String className = fullClassName.substring(fullClassName.indexOf('<')+1, fullClassName.indexOf('>'));
       when(requester.request(query, forName(className))).thenReturn(response);

       assertThat(method.invoke(handlerInstance, query), is(response));
       verify(requester).request(query, forName(className));
       return true;
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
