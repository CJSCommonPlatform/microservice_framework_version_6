package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.common.exception.InvalidSequenceIdException;
import uk.gov.justice.services.eventsourcing.common.exception.InvalidStreamIdException;
import uk.gov.justice.services.eventsourcing.repository.core.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Arrays;
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
        when(eventLogJdbcRepository.findByStreamIdOrderBySequenceIdAsc(STREAM_ID)).thenReturn(Arrays.asList(eventLog).stream());
        when(eventLogConverter.createEnvelope(eventLog)).thenReturn(envelope);

        Stream<JsonEnvelope> streamOfEnvelopes = jdbcEventRepository.getByStreamId(STREAM_ID);

        assertThat(streamOfEnvelopes, not(nullValue()));
        assertThat(streamOfEnvelopes.findFirst().get(), equalTo(envelope));
        verify(logger).trace("Retrieving event stream for {}", STREAM_ID);
    }

    @Test
    public void shouldGetByStreamIdAndSequenceId() throws Exception {
        when(eventLogJdbcRepository.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(STREAM_ID, VERSION_1)).thenReturn(Arrays.asList(eventLog).stream());
        when(eventLogConverter.createEnvelope(eventLog)).thenReturn(envelope);

        Stream<JsonEnvelope> streamOfEnvelopes = jdbcEventRepository.getByStreamIdAndSequenceId(STREAM_ID, VERSION_1);

        assertThat(streamOfEnvelopes, not(nullValue()));
        assertThat(streamOfEnvelopes.findFirst().get(), equalTo(envelope));
        verify(logger).trace("Retrieving event stream for {} at sequence {}", STREAM_ID, VERSION_1);
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
        when(eventLogConverter.createEventLog(envelope, STREAM_ID, VERSION_1)).thenReturn(eventLog);

        jdbcEventRepository.store(envelope, STREAM_ID, VERSION_1);

        verify(eventLogJdbcRepository).insert(eventLog);
        verify(logger).trace("Storing event {} into stream {} at version {}", eventLog.getName(), STREAM_ID, VERSION_1);
    }

    @Test(expected = StoreEventRequestFailedException.class)
    public void shouldThrowExceptionOnDuplicateVersion() throws Exception {
        when(eventLogConverter.createEventLog(envelope, STREAM_ID, VERSION_1)).thenReturn(eventLog);
        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.streamId()).thenReturn(Optional.of(STREAM_ID));
        when(metadata.version()).thenReturn(Optional.of(VERSION_1));

        doThrow(InvalidSequenceIdException.class).when(eventLogJdbcRepository).insert(eventLog);

        jdbcEventRepository.store(envelope, STREAM_ID, VERSION_1);
    }

    @Test
    public void shouldReturnTestSequenceId() {
        when(eventLogJdbcRepository.getLatestSequenceIdForStream(STREAM_ID)).thenReturn(VERSION_1);

        assertThat(jdbcEventRepository.getCurrentSequenceIdForStream(STREAM_ID), equalTo(VERSION_1));
    }

}
