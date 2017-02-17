package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;

import uk.gov.justice.domain.aggregate.TestAggregate;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;
import uk.gov.justice.services.messaging.JsonEnvelope;

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

    @Mock
    private EventStreamManager eventStreamManager;

    @Mock
    private SnapshotService snapshotService;

    private SnapshotAwareEnvelopeEventStream eventStream;


    @Before
    public void setup() {
        eventStream = new SnapshotAwareEnvelopeEventStream(STREAM_ID, eventStreamManager, snapshotService);
    }

    @Test
    public void shouldReturnStreamOfEnvelopes() throws Exception {
        final Stream<JsonEnvelope> stream = Stream.of(envelope().build());
        when(eventStreamManager.read(STREAM_ID)).thenReturn(stream);

        assertThat(eventStream.read(), is(stream));
    }

    @Test
    public void shouldReturnStreamFromVersion() throws Exception {
        final Stream<JsonEnvelope> stream = Stream.of(envelope().build());
        final long version = 10L;
        when(eventStreamManager.readFrom(STREAM_ID, version)).thenReturn(stream);

        assertThat(eventStream.readFrom(version), is(stream));
    }

    @Test
    public void shouldAppendStream() throws Exception {
        final Stream<JsonEnvelope> stream = Stream.of(envelope().build());
        eventStream.append(stream);

        verify(eventStreamManager).append(STREAM_ID, stream);
    }

    @Test
    public void shouldAppendStreamAfterVersion() throws Exception {
        final Stream<JsonEnvelope> stream = Stream.of(envelope().build());
        final long version = 11L;
        eventStream.appendAfter(stream, version);

        verify(eventStreamManager).appendAfter(STREAM_ID, stream, version);
    }

    @Test
    public void shouldReturnCurrentVersion() throws Exception {
        final long version = 11L;
        when(eventStreamManager.getCurrentVersion(STREAM_ID)).thenReturn(version);

        assertThat(eventStream.getCurrentVersion(), is(version));
    }

    @Test
    public void shouldReturnId() throws Exception {
        final UUID actualId = eventStream.getId();

        assertThat(actualId, equalTo(STREAM_ID));
    }

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