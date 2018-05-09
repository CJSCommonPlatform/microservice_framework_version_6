package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidPositionException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidStreamIdException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.common.stream.StreamCloseSpy;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class JdbcBasedEventRepositoryTest {

    private static final UUID STREAM_ID = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb259e");
    private static final long POSITION = 1L;

    @Mock
    private Logger logger;

    @Mock
    private EventJdbcRepository eventJdbcRepository;

    @Mock
    private EventStreamJdbcRepository eventStreamJdbcRepository;

    @Mock
    private EventConverter eventConverter;

    @Mock
    private JsonEnvelope envelope;

    @Mock
    private Metadata metadata;

    @Mock
    private Event event;

    @Mock
    private EventStream eventStream;

    @Mock
    private DefaultEventStreamMetadata eventStreamMetadata;

    @InjectMocks
    private JdbcBasedEventRepository jdbcBasedEventRepository;

    private final static ZonedDateTime TIMESTAMP = new UtcClock().now();

    @Test
    public void shouldGetAllEvents() throws Exception {
        when(eventJdbcRepository.findAll()).thenReturn(Stream.of(event));
        when(eventConverter.envelopeOf(event)).thenReturn(envelope);

        Stream<JsonEnvelope> streamOfEnvelopes = jdbcBasedEventRepository.getEvents();

        assertThat(streamOfEnvelopes, not(nullValue()));
        assertThat(streamOfEnvelopes.findFirst().get(), equalTo(envelope));
        verify(logger).trace("Retrieving all events");
    }

    @Test
    public void shouldGetByStreamId() throws Exception {
        when(eventJdbcRepository.findByStreamIdOrderByPositionAsc(STREAM_ID)).thenReturn(Stream.of(event));
        when(eventConverter.envelopeOf(event)).thenReturn(envelope);

        Stream<JsonEnvelope> streamOfEnvelopes = jdbcBasedEventRepository.getEventsByStreamId(STREAM_ID);

        assertThat(streamOfEnvelopes, not(nullValue()));
        assertThat(streamOfEnvelopes.findFirst().get(), equalTo(envelope));
        verify(logger).trace("Retrieving event stream for {}", STREAM_ID);
    }

    @Test(expected = InvalidStreamIdException.class)
    public void shouldThrowExceptionOnNullStreamId() throws Exception {
        jdbcBasedEventRepository.getEventsByStreamId(null);
    }

    @Test
    public void shouldGetByStreamIdAndSequenceId() throws Exception {
        when(eventJdbcRepository.findByStreamIdFromPositionOrderByPositionAsc(STREAM_ID, POSITION)).thenReturn(Stream.of(event));
        when(eventConverter.envelopeOf(event)).thenReturn(envelope);

        Stream<JsonEnvelope> streamOfEnvelopes = jdbcBasedEventRepository.getEventsByStreamIdFromPosition(STREAM_ID, POSITION);

        assertThat(streamOfEnvelopes, not(nullValue()));
        assertThat(streamOfEnvelopes.findFirst().get(), equalTo(envelope));
        verify(logger).trace("Retrieving event stream for {} at sequence {}", STREAM_ID, POSITION);
    }


    @Test(expected = InvalidStreamIdException.class)
    public void shouldThrowExceptionOnNullStreamIdWhenGettingStreamByStreamIdAndSequence() throws Exception {
        jdbcBasedEventRepository.getEventsByStreamIdFromPosition(null, POSITION);
    }

    @Test(expected = JdbcRepositoryException.class)
    public void shouldThrowExceptionOnNullSequenceIdWhenGettingStreamByStreamIdAndSequence() throws Exception {
        jdbcBasedEventRepository.getEventsByStreamIdFromPosition(STREAM_ID, null);
    }


    @Test
    public void shouldGetStreamOfStreams() throws Exception {
        final UUID streamId1 = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb251e");
        final UUID streamId2 = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb252e");
        final UUID streamId3 = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb253e");

        final Event event1 = eventOf(streamId1);
        final Event event2 = eventOf(streamId2);
        final Event event3 = eventOf(streamId3);
        final JsonEnvelope envelope1 = mock(JsonEnvelope.class);
        final JsonEnvelope envelope2 = mock(JsonEnvelope.class);
        final JsonEnvelope envelope3 = mock(JsonEnvelope.class);


        when(eventJdbcRepository.getStreamIds()).thenReturn(Stream.of(streamId1, streamId2, streamId3));
        when(eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamId1)).thenReturn(Stream.of(event1));
        when(eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamId2)).thenReturn(Stream.of(event2));
        when(eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamId3)).thenReturn(Stream.of(event3));

        when(eventConverter.envelopeOf(event1)).thenReturn(envelope1);
        when(eventConverter.envelopeOf(event2)).thenReturn(envelope2);
        when(eventConverter.envelopeOf(event3)).thenReturn(envelope3);

        final Stream<Stream<JsonEnvelope>> streamOfStreams = jdbcBasedEventRepository.getStreamOfAllEventStreams();

        final List<Stream<JsonEnvelope>> listOfStreams = streamOfStreams.collect(toList());
        assertThat(listOfStreams, hasSize(3));

        assertThat(listOfStreams.get(0).findFirst().get(), is(envelope1));
        assertThat(listOfStreams.get(1).findFirst().get(), is(envelope2));
        assertThat(listOfStreams.get(2).findFirst().get(), is(envelope3));
    }

    @Test
    public void shouldGetActiveStreamOfStreams() throws Exception {
        final UUID streamId1 = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb251e");
        final UUID streamId2 = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb252e");
        final UUID streamId3 = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb253e");

        final Event event1 = eventOf(streamId1);
        final Event event2 = eventOf(streamId2);
        final Event event3 = eventOf(streamId3);
        final JsonEnvelope envelope1 = mock(JsonEnvelope.class);
        final JsonEnvelope envelope2 = mock(JsonEnvelope.class);
        final JsonEnvelope envelope3 = mock(JsonEnvelope.class);

        when(eventStreamJdbcRepository.findActive()).thenReturn(Stream.of(buildEventStreamFor(streamId1, 1L), buildEventStreamFor(streamId2, 2L), buildEventStreamFor(streamId3, 3L)));
        when(eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamId1)).thenReturn(Stream.of(event1));
        when(eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamId2)).thenReturn(Stream.of(event2));
        when(eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamId3)).thenReturn(Stream.of(event3));

        when(eventConverter.envelopeOf(event1)).thenReturn(envelope1);
        when(eventConverter.envelopeOf(event2)).thenReturn(envelope2);
        when(eventConverter.envelopeOf(event3)).thenReturn(envelope3);


        final Stream<Stream<JsonEnvelope>> streamOfStreams = jdbcBasedEventRepository.getStreamOfAllActiveEventStreams();

        final List<Stream<JsonEnvelope>> listOfStreams = streamOfStreams.collect(toList());
        assertThat(listOfStreams, hasSize(3));

        assertThat(listOfStreams.get(0).findFirst().get(), is(envelope1));
        assertThat(listOfStreams.get(1).findFirst().get(), is(envelope2));
        assertThat(listOfStreams.get(2).findFirst().get(), is(envelope3));
    }

    private EventStream buildEventStreamFor(final UUID streamId, final Long sequence) {
        return new EventStream(streamId, sequence, true, TIMESTAMP);
    }

    @Test
    public void shouldCloseAllStreamsOnCloseOfStreamOfStreams() throws Exception {
        final UUID streamId1 = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb251e");
        final UUID streamId2 = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb252e");
        final UUID streamId3 = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb253e");

        final Event event1 = eventOf(streamId1);
        final Event event2 = eventOf(streamId2);
        final Event event3 = eventOf(streamId3);
        final JsonEnvelope envelope1 = mock(JsonEnvelope.class);
        final JsonEnvelope envelope2 = mock(JsonEnvelope.class);
        final JsonEnvelope envelope3 = mock(JsonEnvelope.class);

        StreamCloseSpy streamCloseSpy1 = new StreamCloseSpy();
        StreamCloseSpy streamCloseSpy2 = new StreamCloseSpy();
        StreamCloseSpy streamCloseSpy3 = new StreamCloseSpy();
        StreamCloseSpy streamCloseSpy4 = new StreamCloseSpy();

        when(eventJdbcRepository.getStreamIds()).thenReturn(Stream.of(streamId1, streamId2, streamId3).onClose(streamCloseSpy1));
        when(eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamId1)).thenReturn(Stream.of(event1).onClose(streamCloseSpy2));
        when(eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamId2)).thenReturn(Stream.of(event2).onClose(streamCloseSpy3));
        when(eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamId3)).thenReturn(Stream.of(event3).onClose(streamCloseSpy4));

        when(eventConverter.envelopeOf(event1)).thenReturn(envelope1);
        when(eventConverter.envelopeOf(event2)).thenReturn(envelope2);
        when(eventConverter.envelopeOf(event3)).thenReturn(envelope3);

        final Stream<Stream<JsonEnvelope>> streamOfStreams = jdbcBasedEventRepository.getStreamOfAllEventStreams();
        streamOfStreams.collect(toList());

        streamOfStreams.close();

        assertThat(streamCloseSpy1.streamClosed(), is(true));
        assertThat(streamCloseSpy4.streamClosed(), is(true));
        assertThat(streamCloseSpy2.streamClosed(), is(true));
        assertThat(streamCloseSpy3.streamClosed(), is(true));
    }

    @Test
    public void shouldStoreEventEnvelope() throws Exception {
        final String name = "name123";
        final Event event = new Event(null, STREAM_ID, POSITION, name, null, null, now(), "source");
        when(eventConverter.eventOf(envelope)).thenReturn(event);

        jdbcBasedEventRepository.storeEvent(envelope);

        verify(eventJdbcRepository).insert(event);
        verify(logger).trace("Storing event {} into stream {} at position {}", name, STREAM_ID, POSITION);
    }

    @Test(expected = StoreEventRequestFailedException.class)
    public void shouldThrowExceptionOnDuplicatePosition() throws Exception {
        when(eventConverter.eventOf(envelope)).thenReturn(event);
        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.streamId()).thenReturn(Optional.of(STREAM_ID));
        when(metadata.position()).thenReturn(Optional.of(POSITION));

        doThrow(InvalidPositionException.class).when(eventJdbcRepository).insert(event);

        jdbcBasedEventRepository.storeEvent(envelope);
    }

    @Test
    public void shouldReturnCurrentEventPosition() {
        when(eventJdbcRepository.getStreamSize(STREAM_ID)).thenReturn(POSITION);

        assertThat(jdbcBasedEventRepository.getStreamSize(STREAM_ID), equalTo(POSITION));
    }

    @Test
    public void shouldDeleteStream() {
        jdbcBasedEventRepository.clearEventsForStream(STREAM_ID);

        verify(eventJdbcRepository).clear(STREAM_ID);
    }

    @Test
    public void shouldGetEventStreamBySequenceId() {
        final long POSITION = 1L;

        when(eventStreamJdbcRepository.findEventStreamWithPositionFrom(POSITION)).thenReturn(Stream.of(eventStream));

        final Stream<EventStream> streamOfEnvelopes = eventStreamJdbcRepository.findEventStreamWithPositionFrom(POSITION);
        final List<EventStream> eventStreamObjectList = streamOfEnvelopes.collect(toList());

        assertThat(eventStreamObjectList.size(), is(1));
        assertThat(streamOfEnvelopes, not(nullValue()));
        assertThat(eventStreamObjectList.get(0), equalTo(eventStream));
        verify(eventStreamJdbcRepository).findEventStreamWithPositionFrom(POSITION);
    }

    @Test
    public void shouldMarkEventStreamAsActive() {
        eventStreamJdbcRepository.markActive(STREAM_ID, true);
        verify(eventStreamJdbcRepository).markActive(STREAM_ID, true);
    }


    @Test
    public void shouldGetEventStreamByPosition() {
        long position = 3l;
        final UUID streamId = randomUUID();
        final boolean active = true;
        final ZonedDateTime createdAt = now();
        final EventStream eventStream1 = new EventStream(streamId, position, true, createdAt);
        final Stream<EventStream> eventStreams = Stream.of(eventStream1);

        when(eventStreamJdbcRepository.findEventStreamWithPositionFrom(position)).thenReturn(eventStreams);

        final Stream<EventStreamMetadata> streamOfEnvelopes = jdbcBasedEventRepository.getEventStreamsFromPosition(position);
        final List<EventStreamMetadata> eventStreamMetadataList = streamOfEnvelopes.collect(toList());

        assertThat(eventStreamMetadataList.size(), is(1));
        assertThat(streamOfEnvelopes, not(nullValue()));

        assertThat(eventStreamMetadataList.get(0).getStreamId(), equalTo(streamId));
        assertThat(eventStreamMetadataList.get(0).getPosition(), equalTo(position));
        assertThat(eventStreamMetadataList.get(0).isActive(), equalTo(active));
        assertThat(eventStreamMetadataList.get(0).getCreatedAt(), equalTo(createdAt));

        verify(eventStreamJdbcRepository).findEventStreamWithPositionFrom(position);
    }

    @Test
    public void shouldGetAllEventStreams() {
        long position = 3l;
        final UUID streamId = randomUUID();
        final boolean active = true;
        final ZonedDateTime createdAt = now();
        final EventStream eventStream1 = new EventStream(streamId, position, true, createdAt);
        final Stream<EventStream> eventStreams = Stream.of(eventStream1);

        when(eventStreamJdbcRepository.findAll()).thenReturn(eventStreams);

        final Stream<EventStreamMetadata> streamOfEnvelopes = jdbcBasedEventRepository.getStreams();
        final List<EventStreamMetadata> eventStreamMetadataList = streamOfEnvelopes.collect(toList());

        assertThat(eventStreamMetadataList.size(), is(1));
        assertThat(streamOfEnvelopes, not(nullValue()));

        assertThat(eventStreamMetadataList.get(0).getStreamId(), equalTo(streamId));
        assertThat(eventStreamMetadataList.get(0).getPosition(), equalTo(position));
        assertThat(eventStreamMetadataList.get(0).isActive(), equalTo(active));
        assertThat(eventStreamMetadataList.get(0).getCreatedAt(), equalTo(createdAt));

        verify(eventStreamJdbcRepository).findAll();
    }

    @Test
    public void shouldMarkActive() {
        jdbcBasedEventRepository.markEventStreamActive(STREAM_ID, true);
        verify(eventStreamJdbcRepository).markActive(STREAM_ID, true);
    }

    @Test
    public void shouldReturnStreamPosition() {
        jdbcBasedEventRepository.getStreamPosition(STREAM_ID);
        verify(eventStreamJdbcRepository).getPosition(STREAM_ID);
    }

    @Test
    public void shouldStoreEventStream() {
        jdbcBasedEventRepository.createEventStream(STREAM_ID);
        verify(eventStreamJdbcRepository).insert(STREAM_ID);
    }

    private Event eventOf(final UUID streamId) {
        return new Event(null, streamId, null, null, null, null, null, null);
    }
}
