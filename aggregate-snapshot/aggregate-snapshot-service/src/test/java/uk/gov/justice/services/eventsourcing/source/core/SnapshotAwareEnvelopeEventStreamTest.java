package uk.gov.justice.services.eventsourcing.source.core;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.domain.aggregate.TestAggregate;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SnapshotAwareEnvelopeEventStreamTest {

    private static final UUID STREAM_ID = UUID.randomUUID();
    private static final String EVENT_SOURCE_NAME = "eventSourceName";

    @Mock
    private EventStreamManager eventStreamManager;

    @Mock
    private SnapshotService snapshotService;

    private SnapshotAwareEnvelopeEventStream eventStream;

    @Before
    public void setup() {
        eventStream = new SnapshotAwareEnvelopeEventStream(STREAM_ID, eventStreamManager, snapshotService, EVENT_SOURCE_NAME);
    }

    @Test
    public void shouldReturnStreamOfEnvelopes() throws Exception {
        final JsonEnvelope event = envelope().with(metadataWithDefaults().withVersion(1L)).build();

        when(eventStreamManager.read(STREAM_ID)).thenReturn(Stream.of(event));

        Stream<JsonEnvelope> stream = eventStream.read();

        List<JsonEnvelope> events = stream.collect(toList());
        assertThat(events, hasSize(1));
        assertThat(events.get(0), is(event));
    }

    @Test
    public void shouldReturnStreamFromPosition() throws Exception {

        final long position = 10L;
        final JsonEnvelope event = envelope().with(metadataWithDefaults().withVersion(1L)).build();

        when(eventStreamManager.readFrom(STREAM_ID, position)).thenReturn(Stream.of(event));

        Stream<JsonEnvelope> stream = eventStream.readFrom(position);

        List<JsonEnvelope> events = stream.collect(toList());
        assertThat(events, hasSize(1));
        assertThat(events.get(0), is(event));
    }

    @Test
    public void shouldAppendStream() throws Exception {
        final Stream<JsonEnvelope> stream = Stream.of(envelope().build());
        eventStream.append(stream);

        verify(eventStreamManager).append(STREAM_ID, stream);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldAppendStreamAfterAfterPosition() throws Exception {
        final Stream<JsonEnvelope> stream = Stream.of(envelope().build());
        final long position = 11L;
        eventStream.appendAfter(stream, position);

        verify(eventStreamManager).appendAfter(STREAM_ID, stream, position);
    }

    @Test
    public void shouldReturnSize() throws Exception {
        final long size = 11L;
        when(eventStreamManager.getSize(STREAM_ID)).thenReturn(size);

        assertThat(eventStream.size(), is(size));
    }

    @Test
    public void shouldReturnId() throws Exception {
        final UUID actualId = eventStream.getId();

        assertThat(actualId, equalTo(STREAM_ID));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldAttemptSnapshotCreationOnAppendingEvents() throws Exception {
        final TestAggregate aggregate = new TestAggregate();
        eventStream.registerAggregates(TestAggregate.class, aggregate);

        final long streamVersionAfterAppending = 14L;
        final Stream<JsonEnvelope> streamOfEvents = Stream.of(envelope().build());
        when(eventStreamManager.append(STREAM_ID, streamOfEvents)).thenReturn(streamVersionAfterAppending);

        eventStream.append(streamOfEvents);

        verify(snapshotService).attemptAggregateStore(STREAM_ID, streamVersionAfterAppending, aggregate);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotCreateSnapshotWhenAppendingWithNonConsecutiveTolerance() throws Exception {
        eventStream.registerAggregates(TestAggregate.class, new TestAggregate());

        eventStream.append(Stream.of(envelope().build()), Tolerance.NON_CONSECUTIVE);

        verifyZeroInteractions(snapshotService);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldAttemptSnapshotCreationWhenAppendingWithConsecutiveTolerance() throws Exception {
        final TestAggregate aggregate = new TestAggregate();
        eventStream.registerAggregates(TestAggregate.class, aggregate);

        final long streamVersionAfterAppending = 16L;
        final Stream<JsonEnvelope> streamOfEvents = Stream.of(envelope().build());
        when(eventStreamManager.append(STREAM_ID, streamOfEvents)).thenReturn(streamVersionAfterAppending);

        eventStream.append(streamOfEvents, Tolerance.CONSECUTIVE);

        verify(snapshotService).attemptAggregateStore(STREAM_ID, streamVersionAfterAppending, aggregate);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldAttemptSnapshotCreationOnAppendingEventsFrom() throws Exception {
        final TestAggregate aggregate = new TestAggregate();
        eventStream.registerAggregates(TestAggregate.class, aggregate);

        final long streamVersionToAppendAfter = 16L;
        final long streamVersionAfterAppending = 17L;
        final Stream<JsonEnvelope> streamOfEvents = Stream.of(envelope().build());
        when(eventStreamManager.appendAfter(STREAM_ID, streamOfEvents, streamVersionToAppendAfter)).thenReturn(streamVersionAfterAppending);

        eventStream.appendAfter(streamOfEvents, streamVersionToAppendAfter);

        verify(snapshotService).attemptAggregateStore(STREAM_ID, streamVersionAfterAppending, aggregate);

    }

}
