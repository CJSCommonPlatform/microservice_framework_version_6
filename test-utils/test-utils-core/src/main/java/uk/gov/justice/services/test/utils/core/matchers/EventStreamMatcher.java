package uk.gov.justice.services.test.utils.core.matchers;

import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.stream.Stream;

import com.jayway.jsonpath.matchers.IsJson;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.mockito.ArgumentCaptor;

/**
 * Matches an {@link EventStream} that expects to be appended to using the 'append' or the
 * 'appendAfter' methods.
 *
 * To test the 'append' method of the EventStream, setup and inject a Mock EventStream into the
 * command handler under test.  After the method of the command handler under test is invoked, use
 * the following example assertion:
 *
 * <pre>
 *  {@code
 *      assertThat(eventStream, eventStreamAppendedWith(
 *              jsonEnvelope(
 *                      metadata()
 *                          .withCausationIds(commandId)
 *                          .withName(EVENT_NAME),
 *                      payloadIsJson(allOf(
 *                          withJsonPath("$.recipeId", equalTo(RECIPE_ID.toString())),
 *                          withJsonPath("$.name", equalTo(RECIPE_NAME)),
 *                          withJsonPath("$.glutenFree", equalTo(GULTEN_FREE)),
 *                          withJsonPath("$.ingredients", empty())
 *                      ))))
 *      );
 * }
 * </pre>
 *
 *
 * To test the 'appendAfter' method of the EventStream, setup and inject a Mock EventStream into the
 * command handler under test.  After the method of the command handler under test is invoked, use
 * the following example assertion:
 *
 * <pre>
 *  {@code
 *      final Long version = 4L;
 *
 *      assertThat(eventStream, eventStreamAppendedAfter(version).with(
 *              jsonEnvelope(
 *                      metadata()
 *                          .withCausationIds(commandId)
 *                          .withName(EVENT_NAME),
 *                      payloadIsJson(allOf(
 *                          withJsonPath("$.recipeId", equalTo(RECIPE_ID.toString())),
 *                          withJsonPath("$.name", equalTo(RECIPE_NAME)),
 *                          withJsonPath("$.glutenFree", equalTo(GULTEN_FREE)),
 *                          withJsonPath("$.ingredients", empty())
 *                      ))))
 *      );
 * }
 * </pre>
 *
 *
 * Metadata can also be matched as a JsonObject. For example:
 *
 * <pre>
 *  {@code
 *      assertThat(eventStream, eventStreamAppendedWith(
 *              jsonEnvelope(
 *                      metadata().isJson(allOf(
 *                          withJsonPath("$.context.user", equalTo(USER_ID)),
 *                          withJsonPath("$.name", equalTo(EVENT_NAME)))
 *                      payloadIsJson(allOf(
 *                          withJsonPath("$.recipeId", equalTo(RECIPE_ID.toString())),
 *                          withJsonPath("$.name", equalTo(RECIPE_NAME)),
 *                          withJsonPath("$.glutenFree", equalTo(GULTEN_FREE)),
 *                          withJsonPath("$.ingredients", empty())
 *                      ))))
 *      );
 * }
 * </pre>
 *
 * This makes use of {@link IsJson} to achieve Json matching in the payload.
 */

public class EventStreamMatcher extends TypeSafeDiagnosingMatcher<EventStream> {

    private JsonEnvelopeStreamMatcher jsonEnvelopeStreamMatcher;
    private Optional<Long> version = Optional.empty();

    public static EventStreamMatcher eventStreamAppendedWith(final JsonEnvelopeStreamMatcher jsonEnvelopeStreamMatcher) {
        return new EventStreamMatcher().with(jsonEnvelopeStreamMatcher);
    }

    public static EventStreamMatcher eventStreamAppendedAfter(final Long version) {
        return new EventStreamMatcher().afterVersion(version);
    }

    public EventStreamMatcher with(final JsonEnvelopeStreamMatcher jsonEnvelopeStreamMatcher) {
        this.jsonEnvelopeStreamMatcher = jsonEnvelopeStreamMatcher;
        return this;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("EventStream appended with (");
        description.appendDescriptionOf(jsonEnvelopeStreamMatcher);
        description.appendText(")");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean matchesSafely(final EventStream eventStream, final Description description) {
        final ArgumentCaptor<Stream> argumentCaptor = forClass(Stream.class);

        try {

            if (version.isPresent()) {
                verify(eventStream).appendAfter(argumentCaptor.capture(), eq(version.get()));
            } else {
                verify(eventStream).append(argumentCaptor.capture());
            }

            final Stream<JsonEnvelope> jsonEnvelopeStream = argumentCaptor.getValue();

            if (!jsonEnvelopeStreamMatcher.matchesSafely(jsonEnvelopeStream, description)) {
                jsonEnvelopeStreamMatcher.describeMismatch(jsonEnvelopeStream, description);
                return false;
            }

        } catch (EventStreamException e) {
            description.appendText("Unable to match due to, EventStreamException :");
            description.appendValue(e);
            return false;
        }

        return true;
    }

    private EventStreamMatcher afterVersion(final Long version) {
        this.version = Optional.of(version);
        return this;
    }
}
