package uk.gov.justice.services.eventsourcing.source.core;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.DefaultEventStreamMetadata;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventStreamMetadata;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JdbcBasedEventSourceTest {

    private static final UUID STREAM_ID = randomUUID();

    @InjectMocks
    JdbcBasedEventSource eventSource;

    @Mock
    private EventStreamManager eventStreamManager;

    @Mock
    private EventRepository eventRepository;

    @Test
    public void shouldReturnEventStream() {
        EnvelopeEventStream eventStream = (EnvelopeEventStream) eventSource.getStreamById(STREAM_ID);

        assertThat(eventStream.getId(), equalTo(STREAM_ID));
    }

    @Test
    public void shouldGetEventStreamsByPosition() {
        final UUID streamId = randomUUID();
        final long position = 1L;

        final Stream<EventStreamMetadata> eventStreamObjectStream = Stream.of(new DefaultEventStreamMetadata(streamId, position, true, now()));
        when(eventRepository.getEventStreamsFromPosition(position)).thenReturn(eventStreamObjectStream);

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
        final long position = 9L;

        when(eventRepository.getEventStreamsFromPosition(position)).thenReturn(Stream.empty());

        final Stream<uk.gov.justice.services.eventsourcing.source.core.EventStream> eventStreams = eventSource.getStreamsFrom(position);
        List<uk.gov.justice.services.eventsourcing.source.core.EventStream> eventStreamList = eventStreams.collect(toList());

        assertThat(eventStreamList.size(), is(0));
    }

    @Test
    public void shouldGetStreamsWithoutUsingPosition() throws Exception {

        final UUID streamId = randomUUID();
        final long position = 1L;

        final Stream<EventStreamMetadata> eventStreamObjectStream = Stream.of(new DefaultEventStreamMetadata(streamId, position, true, now()));
        when(eventRepository.getStreams()).thenReturn(eventStreamObjectStream);

        final Stream<uk.gov.justice.services.eventsourcing.source.core.EventStream> eventStreams = eventSource.getStreams();
        List<uk.gov.justice.services.eventsourcing.source.core.EventStream> eventStreamList = eventStreams.collect(toList());

        assertThat(eventStreamList.size(), is(1));
        assertThat(eventStreamList.get(0), instanceOf(uk.gov.justice.services.eventsourcing.source.core.EventStream.class));
        assertThat(eventStreamList.get(0), instanceOf(EnvelopeEventStream.class));
        assertThat(eventStreamList.get(0).getId(), is(streamId));
        assertThat(eventStreamList.get(0).getPosition(), is(position));
    }

    @Test
    public void shouldReturnEventStreamName() {
        final String eventSourceName = "eventSourceName";
        setField(eventSource, "name", eventSourceName);

        final EnvelopeEventStream eventStream = (EnvelopeEventStream) eventSource.getStreamById(STREAM_ID);

        assertThat(eventStream.getName(), equalTo(eventSourceName));
    }
}
