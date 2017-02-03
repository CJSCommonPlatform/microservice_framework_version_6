package uk.gov.justice.services.test.utils.core.matchers;

import static java.lang.String.format;
import static org.mockito.Answers.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import org.hamcrest.Description;

/**
 * Matcher for asserting that a handler method is a simple sender pass-though.
 */
public class SenderPassThroughMatcher extends AbstractHandlerMethodMatcher {

    private final String methodName;

    private SenderPassThroughMatcher(final String methodName) {
        this.methodName = methodName;
    }

    public static SenderPassThroughMatcher hasSenderPassThroughMethod(final String methodName) {
        return new SenderPassThroughMatcher(methodName);
    }

    @Override
    protected boolean matchesSafely(final Class<?> handlerClass, final Description description) {

        final Optional<Method> method = getHandlerMethod(methodName, handlerClass, description);

        try {
            return method.isPresent() && isSenderPassThrough(handlerClass, method.get());
        } catch (final Exception ex) {
            description.appendText("Method ")
                    .appendValue(methodName)
                    .appendText(" of class ")
                    .appendValue(handlerClass)
                    .appendText(" is not a sender pass-through method");
            return false;
        }
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Method ")
                .appendValue(methodName)
                .appendText(" should be a sender pass-through method");
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
        verify(sender, times(ONCE)).send(command);

        return true;
    }
}
