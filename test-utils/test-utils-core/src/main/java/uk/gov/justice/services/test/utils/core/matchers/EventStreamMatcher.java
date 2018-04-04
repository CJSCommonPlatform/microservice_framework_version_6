package uk.gov.justice.services.test.utils.core.matchers;

import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.matchers.EmptyStreamMatcher.isEmptyStream;

import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.Tolerance;
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
 * Match an empty EventStream:
 *
 * <pre>
 *  {@code
 *      assertThat(eventStream, eventStreamWithEmptyStream());
 * }
 * </pre>
 *
 *
 * This makes use of {@link IsJson} to achieve Json matching in the payload.
 */

public class EventStreamMatcher extends TypeSafeDiagnosingMatcher<EventStream> {

    private Optional<JsonEnvelopeStreamMatcher> jsonEnvelopeStreamMatcher = Optional.empty();
    private Optional<EmptyStreamMatcher> emptyStreamMatcher = Optional.empty();
    private Optional<Long> version = Optional.empty();
    private Optional<Tolerance> tolerance = Optional.empty();

    public static EventStreamMatcher eventStreamAppendedWith(final JsonEnvelopeStreamMatcher jsonEnvelopeStreamMatcher) {
        return new EventStreamMatcher().with(jsonEnvelopeStreamMatcher);
    }

    public static EventStreamMatcher eventStreamAppendedAfter(final Long version) {
        return new EventStreamMatcher().afterVersion(version);
    }

    public static EventStreamMatcher eventStreamWithEmptyStream() {
        return new EventStreamMatcher().withEmptyStream();
    }

    public EventStreamMatcher with(final JsonEnvelopeStreamMatcher jsonEnvelopeStreamMatcher) {
        this.jsonEnvelopeStreamMatcher = Optional.of(jsonEnvelopeStreamMatcher);
        return this;
    }

    public EventStreamMatcher withEmptyStream() {
        this.emptyStreamMatcher = Optional.of(isEmptyStream());
        return this;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("EventStream appended with (");
        jsonEnvelopeStreamMatcher.ifPresent(description::appendDescriptionOf);
        emptyStreamMatcher.ifPresent(description::appendDescriptionOf);
        description.appendText(")");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean matchesSafely(final EventStream eventStream, final Description description) {
        final ArgumentCaptor<Stream> argumentCaptor = forClass(Stream.class);

        try {

            if (version.isPresent()) {
                verify(eventStream).appendAfter(argumentCaptor.capture(), eq(version.get()));
            } else if (tolerance.isPresent()) {
                verify(eventStream).append(argumentCaptor.capture(), eq(tolerance.get()));
            } else {
                verify(eventStream).append(argumentCaptor.capture());
            }

            final Stream<JsonEnvelope> jsonEnvelopeStream = argumentCaptor.getValue();

            if (jsonEnvelopeStreamMatcher.isPresent()) {

                if (!jsonEnvelopeStreamMatcher.get().matches(jsonEnvelopeStream)) {
                    jsonEnvelopeStreamMatcher.get().describeMismatch(jsonEnvelopeStream, description);
                    return false;
                }

            } else {

                if (emptyStreamMatcher.isPresent() && !emptyStreamMatcher.get().matches(jsonEnvelopeStream)) {
                    emptyStreamMatcher.get().describeMismatch(jsonEnvelopeStream, description);
                    return false;
                }

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

    public EventStreamMatcher withToleranceOf(final Tolerance tolerance) {
        this.tolerance = Optional.of(tolerance);
        return this;
    }
}
