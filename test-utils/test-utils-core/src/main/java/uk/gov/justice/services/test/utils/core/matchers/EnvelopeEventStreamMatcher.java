package uk.gov.justice.services.test.utils.core.matchers;

import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.matchers.EmptyStreamMatcher.isEmptyStream;

import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.Tolerance;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;

import java.util.Optional;
import java.util.stream.Stream;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.mockito.ArgumentCaptor;

public class EnvelopeEventStreamMatcher<T> extends TypeSafeDiagnosingMatcher<EventStream> {

    private Optional<EnvelopeStreamMatcher> envelopeStreamMatcher = Optional.empty();
    private Optional<EmptyStreamMatcher> emptyStreamMatcher = Optional.empty();
    private Optional<Long> version = Optional.empty();
    private Optional<Tolerance> tolerance = Optional.empty();

    @SuppressWarnings("unchecked")
    public static <T> EnvelopeEventStreamMatcher<T> eventStreamAppendedWith(final EnvelopeStreamMatcher envelopeStreamMatcher, final Class<T> classType) {
        return new EnvelopeEventStreamMatcher<T>().with(envelopeStreamMatcher);
    }

    public static EnvelopeEventStreamMatcher eventStreamAppendedAfter(final Long version) {
        return new EnvelopeEventStreamMatcher().afterVersion(version);
    }

    public static EnvelopeEventStreamMatcher eventStreamWithEmptyStream() {
        return new EnvelopeEventStreamMatcher().withEmptyStream();
    }

    public EnvelopeEventStreamMatcher with(final EnvelopeStreamMatcher envelopeStreamMatcher) {
        this.envelopeStreamMatcher = Optional.of(envelopeStreamMatcher);
        return this;
    }

    public EnvelopeEventStreamMatcher withEmptyStream() {
        this.emptyStreamMatcher = Optional.of(isEmptyStream());
        return this;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("EventStream appended with (");
        envelopeStreamMatcher.ifPresent(description::appendDescriptionOf);
        emptyStreamMatcher.ifPresent(description::appendDescriptionOf);
        description.appendText(")");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean matchesSafely(final EventStream eventStream, final Description description) {
        final ArgumentCaptor<Stream> argumentCaptor = forClass(Stream.class);
        final ArgumentCaptor<Class> classArgumentCaptor = forClass(Class.class);

        try {

            if (version.isPresent()) {
                verify(eventStream).appendAfter(argumentCaptor.capture(), eq(version.get()), classArgumentCaptor.capture());
            } else if (tolerance.isPresent()) {
                verify(eventStream).append(argumentCaptor.capture(), eq(tolerance.get()), classArgumentCaptor.capture());
            } else {
                verify(eventStream).append(argumentCaptor.capture(), classArgumentCaptor.capture());
            }

            final Stream<Envelope<T>> envelopeStream = argumentCaptor.getValue();

            if (envelopeStreamMatcher.isPresent() && !envelopeStreamMatcher.get().matches(envelopeStream)) {
                envelopeStreamMatcher.get().describeMismatch(eventStream, description);
                return false;
            } else if (emptyStreamMatcher.isPresent() && !emptyStreamMatcher.get().matches(envelopeStream)) {
                    emptyStreamMatcher.get().describeMismatch(envelopeStream, description);
                    return false;
            }

        } catch (EventStreamException e) {
            description.appendText("Unable to match due to, EventStreamException :");
            description.appendValue(e);
            return false;
        }

        return true;
    }

    private EnvelopeEventStreamMatcher afterVersion(final Long version) {
        this.version = Optional.of(version);
        return this;
    }

    public EnvelopeEventStreamMatcher withToleranceOf(final Tolerance tolerance) {
        this.tolerance = Optional.of(tolerance);
        return this;
    }
}
