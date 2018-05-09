package uk.gov.justice.services.eventsourcing.source.core;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.DefaultEventStreamMetadata;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventStreamMetadata;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcBasedEventRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SnapshotAwareEventSourceTest {

    private static final UUID STREAM_ID = randomUUID();

    @Mock
    private EventStreamManager eventStreamManager;

    @Mock
    private JdbcBasedEventRepository eventRepository;

    @InjectMocks
    SnapshotAwareEventSource eventSource;

    @Test
    public void shouldReturnEventStream() {
        EnvelopeEventStream eventStream = (EnvelopeEventStream) eventSource.getStreamById(STREAM_ID);

        assertThat(eventStream.getId(), equalTo(STREAM_ID));
    }

    @Test
    public void shouldGetEventStreamsFromPosition() {
        final UUID streamId = randomUUID();
        final long position = 1L;

        final Stream<EventStreamMetadata> eventStreamMetadatas = Stream.of(new DefaultEventStreamMetadata(streamId, position, true, now()));
        when(eventRepository.getEventStreamsFromPosition(position)).thenReturn(eventStreamMetadatas);
        when(eventStreamManager.getStreamPosition(streamId)).thenReturn(1l);

        final Stream<uk.gov.justice.services.eventsourcing.source.core.EventStream> eventStreams = eventSource.getStreamsFrom(position);
        List<uk.gov.justice.services.eventsourcing.source.core.EventStream> eventStreamList = eventStreams.collect(toList());

        assertThat(eventStreamList.size(), is(1));
        assertThat(eventStreamList.get(0), instanceOf(uk.gov.justice.services.eventsourcing.source.core.EventStream.class));
        assertThat(eventStreamList.get(0), instanceOf(EnvelopeEventStream.class));
        assertThat(eventStreamList.get(0).getId(), is(streamId));
        assertThat(eventStreamList.get(0).getPosition(), is(position));
    }

    @Test
    public void shouldReturnEmptyStream() {
        final long sequenceNumber = 9L;

        when(eventRepository.getEventStreamsFromPosition(sequenceNumber)).thenReturn(Stream.empty());

        final Stream<uk.gov.justice.services.eventsourcing.source.core.EventStream> eventStreams = eventSource.getStreamsFrom(sequenceNumber);
        List<uk.gov.justice.services.eventsourcing.source.core.EventStream> eventStreamList = eventStreams.collect(toList());

        assertThat(eventStreamList.size(), is(0));
    }
}