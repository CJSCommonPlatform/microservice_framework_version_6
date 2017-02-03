package uk.gov.justice.services.test.utils.core.matchers;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Answers.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;

import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import org.hamcrest.Description;

/**
 * Matcher for asserting that a handler method is a simple requester pass-though.
 */
public class RequesterPassThroughMatcher extends AbstractHandlerMethodMatcher {

    private final String methodName;

    private RequesterPassThroughMatcher(final String methodName) {
        this.methodName = methodName;
    }

    public static RequesterPassThroughMatcher hasRequesterPassThroughMethod(final String methodName) {
        return new RequesterPassThroughMatcher(methodName);
    }

    @Override
    protected boolean matchesSafely(final Class<?> handlerClass, final Description description) {

        final Optional<Method> method = getHandlerMethod(methodName, handlerClass, description);

        try {
            return method.isPresent() && isRequesterPassThrough(handlerClass, method.get());
        } catch (final Exception ex) {
            description.appendText("Method ")
                    .appendValue(methodName)
                    .appendText(" of class ")
                    .appendValue(handlerClass)
                    .appendText(" is not a requester pass-through method");
            return false;
        }
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Method ")
                .appendValue(methodName)
                .appendText(" should be a requester pass-through method");
    }

    private boolean isRequesterPassThrough(final Class<?> handlerClass, final Method method) throws Exception {
        final Requester requester = mock(Requester.class,
                withSettings()
                        .name(format("%s.requester.request", method.getName()))
                        .invocationListeners(SKIP_JSON_VALIDATION_LISTENER)
                        .defaultAnswer(RETURNS_DEFAULTS.get()));
        final JsonEnvelope query = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope response = envelope().with(metadataWithDefaults()).build();
        final Object handlerInstance = handlerClass.newInstance();

        final Field requesterField = findField(handlerClass, Requester.class);
        requesterField.setAccessible(true);
        requesterField.set(handlerInstance, requester);

        when(requester.request(query)).thenReturn(response);

        assertThat(method.invoke(handlerInstance, query), is(response));
        verify(requester, times(ONCE)).request(query);

        return true;
    }
}
