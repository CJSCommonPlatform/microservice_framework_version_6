package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static java.time.ZonedDateTime.now;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidStreamIdException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.common.stream.StreamCloseSpy;

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
public class JdbcEventRepositoryTest {

    private static final UUID STREAM_ID = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb259e");
    private static final long VERSION_1 = 1L;

    @Mock
    private Logger logger;

    @Mock
    private Stream<EventLog> eventLogs;

    @Mock
    private EventLogJdbcRepository eventLogJdbcRepository;

    @Mock
    private EventLogConverter eventLogConverter;

    @Mock
    private JsonEnvelope envelope;

    @Mock
    private Metadata metadata;

    @Mock
    private EventLog eventLog;

    @InjectMocks
    private JdbcEventRepository jdbcEventRepository;

    @Test
    public void shouldGetByStreamId() throws Exception {
        when(eventLogJdbcRepository.findByStreamIdOrderBySequenceIdAsc(STREAM_ID)).thenReturn(Stream.of(eventLog));
        when(eventLogConverter.envelopeOf(eventLog)).thenReturn(envelope);

        Stream<JsonEnvelope> streamOfEnvelopes = jdbcEventRepository.getByStreamId(STREAM_ID);

        assertThat(streamOfEnvelopes, not(nullValue()));
        assertThat(streamOfEnvelopes.findFirst().get(), equalTo(envelope));
        verify(logger).trace("Retrieving event stream for {}", STREAM_ID);
    }

    @Test
    public void shouldGetByStreamIdAndSequenceId() throws Exception {
        when(eventLogJdbcRepository.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(STREAM_ID, VERSION_1)).thenReturn(Stream.of(eventLog));
        when(eventLogConverter.envelopeOf(eventLog)).thenReturn(envelope);

        Stream<JsonEnvelope> streamOfEnvelopes = jdbcEventRepository.getByStreamIdAndSequenceId(STREAM_ID, VERSION_1);

        assertThat(streamOfEnvelopes, not(nullValue()));
        assertThat(streamOfEnvelopes.findFirst().get(), equalTo(envelope));
        verify(logger).trace("Retrieving event stream for {} at sequence {}", STREAM_ID, VERSION_1);
    }

    @Test
    public void shouldGetAll() throws Exception {
        when(eventLogJdbcRepository.findAll()).thenReturn(Stream.of(eventLog));
        when(eventLogConverter.envelopeOf(eventLog)).thenReturn(envelope);

        Stream<JsonEnvelope> streamOfEnvelopes = jdbcEventRepository.getAll();

        assertThat(streamOfEnvelopes, not(nullValue()));
        assertThat(streamOfEnvelopes.findFirst().get(), equalTo(envelope));
        verify(logger).trace("Retrieving all events");
    }

    @Test
    public void shouldGetStreamOfStreams() throws Exception {
        final UUID streamId1 = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb251e");
        final UUID streamId2 = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb252e");
        final UUID streamId3 = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb253e");

        final EventLog event1 = eventLogOf(streamId1);
        final EventLog event2 = eventLogOf(streamId2);
        final EventLog event3 = eventLogOf(streamId3);
        final JsonEnvelope envelope1 = envelope().build();
        final JsonEnvelope envelope2 = envelope().build();
        final JsonEnvelope envelope3 = envelope().build();


        when(eventLogJdbcRepository.getStreamIds()).thenReturn(Stream.of(streamId1, streamId2, streamId3));
        when(eventLogJdbcRepository.findByStreamIdOrderBySequenceIdAsc(streamId1)).thenReturn(Stream.of(event1));
        when(eventLogJdbcRepository.findByStreamIdOrderBySequenceIdAsc(streamId2)).thenReturn(Stream.of(event2));
        when(eventLogJdbcRepository.findByStreamIdOrderBySequenceIdAsc(streamId3)).thenReturn(Stream.of(event3));

        when(eventLogConverter.envelopeOf(event1)).thenReturn(envelope1);
        when(eventLogConverter.envelopeOf(event2)).thenReturn(envelope2);
        when(eventLogConverter.envelopeOf(event3)).thenReturn(envelope3);

        final Stream<Stream<JsonEnvelope>> streamOfStreams = jdbcEventRepository.getStreamOfAllEventStreams();

        final List<Stream<JsonEnvelope>> listOfStreams = streamOfStreams.collect(toList());
        assertThat(listOfStreams, hasSize(3));

        assertThat(listOfStreams.get(0).findFirst().get(), is(envelope1));
        assertThat(listOfStreams.get(1).findFirst().get(), is(envelope2));
        assertThat(listOfStreams.get(2).findFirst().get(), is(envelope3));
    }

    @Test
    public void shouldCloseAllStreamsOnCloseOfStreamOfStreams() throws Exception {
        final UUID streamId1 = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb251e");
        final UUID streamId2 = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb252e");
        final UUID streamId3 = UUID.fromString("4b4e80a0-76f7-476c-b75b-527e38fb253e");

        final EventLog event1 = eventLogOf(streamId1);
        final EventLog event2 = eventLogOf(streamId2);
        final EventLog event3 = eventLogOf(streamId3);
        final JsonEnvelope envelope1 = envelope().build();
        final JsonEnvelope envelope2 = envelope().build();
        final JsonEnvelope envelope3 = envelope().build();

        StreamCloseSpy streamCloseSpy1 = new StreamCloseSpy();
        StreamCloseSpy streamCloseSpy2 = new StreamCloseSpy();
        StreamCloseSpy streamCloseSpy3 = new StreamCloseSpy();
        StreamCloseSpy streamCloseSpy4 = new StreamCloseSpy();

        when(eventLogJdbcRepository.getStreamIds()).thenReturn(Stream.of(streamId1, streamId2, streamId3).onClose(streamCloseSpy1));
        when(eventLogJdbcRepository.findByStreamIdOrderBySequenceIdAsc(streamId1)).thenReturn(Stream.of(event1).onClose(streamCloseSpy2));
        when(eventLogJdbcRepository.findByStreamIdOrderBySequenceIdAsc(streamId2)).thenReturn(Stream.of(event2).onClose(streamCloseSpy3));
        when(eventLogJdbcRepository.findByStreamIdOrderBySequenceIdAsc(streamId3)).thenReturn(Stream.of(event3).onClose(streamCloseSpy4));

        when(eventLogConverter.envelopeOf(event1)).thenReturn(envelope1);
        when(eventLogConverter.envelopeOf(event2)).thenReturn(envelope2);
        when(eventLogConverter.envelopeOf(event3)).thenReturn(envelope3);

        final Stream<Stream<JsonEnvelope>> streamOfStreams = jdbcEventRepository.getStreamOfAllEventStreams();
        streamOfStreams.collect(toList());

        streamOfStreams.close();

        assertThat(streamCloseSpy1.streamClosed(), is(true));
        assertThat(streamCloseSpy4.streamClosed(), is(true));
        assertThat(streamCloseSpy2.streamClosed(), is(true));
        assertThat(streamCloseSpy3.streamClosed(), is(true));
    }


    @Test(expected = InvalidStreamIdException.class)
    public void shouldThrowExceptionOnNullStreamId() throws Exception {
        jdbcEventRepository.getByStreamId(null);
    }


    @Test(expected = InvalidStreamIdException.class)
    public void shouldThrowExceptionOnNullStreamIdWhenGettingStreamByStreamIdAndSequence() throws Exception {
        jdbcEventRepository.getByStreamIdAndSequenceId(null, VERSION_1);
    }

    @Test(expected = JdbcRepositoryException.class)
    public void shouldThrowExceptionOnNullSequenceIdWhenGettingStreamByStreamIdAndSequence() throws Exception {
        jdbcEventRepository.getByStreamIdAndSequenceId(STREAM_ID, null);
    }

    @Test
    public void shouldStoreEnvelope() throws Exception {
        final String name = "name123";
        final EventLog eventLog = new EventLog(null, STREAM_ID, VERSION_1, name, null, null, now());
        when(eventLogConverter.eventLogOf(envelope)).thenReturn(eventLog);

        jdbcEventRepository.store(envelope);

        verify(eventLogJdbcRepository).insert(eventLog);
        verify(logger).trace("Storing event {} into stream {} at version {}", name, STREAM_ID, VERSION_1);
    }

    @Test(expected = StoreEventRequestFailedException.class)
    public void shouldThrowExceptionOnDuplicateVersion() throws Exception {
        when(eventLogConverter.eventLogOf(envelope)).thenReturn(eventLog);
        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.streamId()).thenReturn(Optional.of(STREAM_ID));
        when(metadata.version()).thenReturn(Optional.of(VERSION_1));

        doThrow(InvalidSequenceIdException.class).when(eventLogJdbcRepository).insert(eventLog);

        jdbcEventRepository.store(envelope);
    }

    @Test
    public void shouldReturnTestSequenceId() {
        when(eventLogJdbcRepository.getLatestSequenceIdForStream(STREAM_ID)).thenReturn(VERSION_1);

        assertThat(jdbcEventRepository.getCurrentSequenceIdForStream(STREAM_ID), equalTo(VERSION_1));
    }

    private EventLog eventLogOf(final UUID streamId) {
        return new EventLog(null, streamId, null, null, null, null, null);
    }

}
