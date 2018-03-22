package uk.gov.justice.services.eventsourcing.source.api.service.core;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.first;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.head;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.position;

import uk.gov.justice.services.eventsourcing.source.core.EnvelopeEventStream;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.EventStreamManager;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamServiceTest {

    @Mock
    private EventStreamManager eventStreamManager;

    @Mock
    private EventSource eventSource;

    @InjectMocks
    private EventStreamService service;

    @Test
    public void shouldReturnHeadEvents() throws Exception {

        final long pageSize = 2L;

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();

        final EventStream eventStream1 = buildEventStreamOf(streamId1, 1L);
        final EventStream eventStream2 = buildEventStreamOf(streamId2, 2L);
        final EventStream eventStream3 = buildEventStreamOf(streamId3, 3L);
        final EventStream eventStream4 = buildEventStreamOf(streamId4, 4L);

        final Stream.Builder<EventStream> eventStreamBuilder = Stream.builder();

        eventStreamBuilder.add(eventStream1);
        eventStreamBuilder.add(eventStream2);
        eventStreamBuilder.add(eventStream3);
        eventStreamBuilder.add(eventStream4);

        final Stream.Builder<EventStream> eventStreamBuilderFrom3 = Stream.builder();
        eventStreamBuilderFrom3.add(eventStream3);
        eventStreamBuilderFrom3.add(eventStream4);

        when(eventSource.getStreamsFrom(1)).thenReturn(eventStreamBuilder.build());
        when(eventSource.getStreamsFrom(3)).thenReturn(eventStreamBuilderFrom3.build());
        when(eventStreamManager.getStreamPosition(streamId3)).thenReturn(3L);
        when(eventStreamManager.getStreamPosition(streamId4)).thenReturn(4L);

        final List<EventStreamEntry> entries = service.eventStreams(head(), BACKWARD, pageSize);

        assertThat(entries, hasSize(2));

        assertThat(entries.get(0).getStreamId(), is(streamId4.toString()));

        assertThat(entries.get(0).getSequenceNumber(), is(4L));

        assertThat(entries.get(1).getStreamId(), is(streamId3.toString()));

        assertThat(entries.get(1).getSequenceNumber(), is(3L));
    }

    @Test
    public void shouldReturnFirstEvents() throws Exception {
        final long pageSize = 2L;

        final Stream.Builder<EventStream> eventStreamBuilder = Stream.builder();

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();

        final EventStream eventStream1 = buildEventStreamOf(streamId1, 1L);
        final EventStream eventStream2 = buildEventStreamOf(streamId2, 2L);

        eventStreamBuilder.add(eventStream1);
        eventStreamBuilder.add(eventStream2);

        final Stream.Builder<EventStream> eventStreamBuilderFrom1 = Stream.builder();
        eventStreamBuilderFrom1.add(eventStream1);
        eventStreamBuilderFrom1.add(eventStream2);

        when(eventSource.getStreamsFrom(1)).thenReturn(eventStreamBuilder.build());
        when(eventStreamManager.getStreamPosition(streamId1)).thenReturn(1L);
        when(eventStreamManager.getStreamPosition(streamId2)).thenReturn(2L);

        final List<EventStreamEntry> eventStreamEntries = service.eventStreams(first(), FORWARD, pageSize);

        assertThat(eventStreamEntries, hasSize(2));

        assertThat(eventStreamEntries.get(0).getStreamId(), is(streamId2.toString()));

        assertThat(eventStreamEntries.get(0).getSequenceNumber(), is(2L));

        assertThat(eventStreamEntries.get(1).getStreamId(), is(streamId1.toString()));

        assertThat(eventStreamEntries.get(1).getSequenceNumber(), is(1L));
    }

    @Test
    public void shouldReturnPreviousEvents() throws Exception {

        final long pageSize = 2L;

        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();

        final Stream.Builder<EventStream> eventStreamBuilder = Stream.builder();

        final EventStream eventStream2 = buildEventStreamOf(streamId2, 2L);
        final EventStream eventStream3 = buildEventStreamOf(streamId3, 3L);

        eventStreamBuilder.add(eventStream2);
        eventStreamBuilder.add(eventStream3);

        final long position = 3L;

        when(eventSource.getStreamsFrom(2L)).thenReturn(eventStreamBuilder.build());
        when(eventStreamManager.getStreamPosition(streamId3)).thenReturn(3L);
        when(eventStreamManager.getStreamPosition(streamId2)).thenReturn(2L);

        final List<EventStreamEntry> eventEntries = service.eventStreams(position(position), BACKWARD, pageSize);

        assertThat(eventEntries, hasSize(2));

        assertThat(eventEntries.get(0).getStreamId(), is(streamId3.toString()));

        assertThat(eventEntries.get(0).getSequenceNumber(), is(3L));

        assertThat(eventEntries.get(1).getStreamId(), is(streamId2.toString()));

        assertThat(eventEntries.get(1).getSequenceNumber(), is(2L));
    }

    @Test
    public void shouldReturnNextEvents() throws Exception {


        final long pageSize = 2L;

        final UUID streamId5 = randomUUID();
        final UUID streamId4 = randomUUID();

        final Stream.Builder<EventStream> eventStreamBuilder = Stream.builder();

        final EventStream eventStream5 = buildEventStreamOf(streamId5, 5L);
        final EventStream eventStream4 = buildEventStreamOf(streamId4, 4L);

        eventStreamBuilder.add(eventStream4);
        eventStreamBuilder.add(eventStream5);

        final long position = 3L;

        when(eventSource.getStreamsFrom(position)).thenReturn(eventStreamBuilder.build());
        when(eventStreamManager.getStreamPosition(streamId5)).thenReturn(5L);
        when(eventStreamManager.getStreamPosition(streamId4)).thenReturn(4L);

        final List<EventStreamEntry> eventEntries = service.eventStreams(position(position), FORWARD, pageSize);

        assertThat(eventEntries, hasSize(2));

        assertThat(eventEntries.get(0).getStreamId(), is(streamId5.toString()));

        assertThat(eventEntries.get(0).getSequenceNumber(), is(5L));

        assertThat(eventEntries.get(1).getStreamId(), is(streamId4.toString()));

        assertThat(eventEntries.get(1).getSequenceNumber(), is(4L));
    }

    @Test
    public void shouldReturnRecordExists() throws Exception {
        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();

        final Stream.Builder<EventStream> eventStreamBuilder = Stream.builder();

        final EventStream eventStream1 = buildEventStreamOf(streamId1, 1L);
        final EventStream eventStream2 = buildEventStreamOf(streamId2, 2L);
        final EventStream eventStream3 = buildEventStreamOf(streamId3, 3L);
        final EventStream eventStream4 = buildEventStreamOf(streamId4, 4L);


        eventStreamBuilder.add(eventStream1);
        eventStreamBuilder.add(eventStream2);
        eventStreamBuilder.add(eventStream3);
        eventStreamBuilder.add(eventStream4);


        when(eventSource.getStreamsFrom(1)).thenReturn(eventStreamBuilder.build());

      //  assertThat(service.eventStreamExists(1), is(true));
    }

    private EnvelopeEventStream buildEventStreamOf(final UUID streamId, final long sequenceNumber) {
        return new EnvelopeEventStream(streamId, sequenceNumber, eventStreamManager);
    }

}