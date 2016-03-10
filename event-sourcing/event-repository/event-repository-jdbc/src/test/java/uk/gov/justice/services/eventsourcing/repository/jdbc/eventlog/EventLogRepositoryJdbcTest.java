package uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.EventLogRepositoryException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.JdbcEventLogRepository.COL_METADATA;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.JdbcEventLogRepository.COL_NAME;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.JdbcEventLogRepository.COL_PAYLOAD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.JdbcEventLogRepository.COL_SEQUENCE_ID;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.JdbcEventLogRepository.COL_STREAM_ID;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.JdbcEventLogRepository.JNDI_APP_NAME_LOOKUP;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.JdbcEventLogRepository.JNDI_DS_EVENT_STORE_PATTERN;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.JdbcEventLogRepository.PRIMARY_KEY_ID;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.JdbcEventLogRepository.SQL_FIND_BY_STREAM_ID;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.JdbcEventLogRepository.SQL_FIND_BY_STREAM_ID_AND_SEQUENCE_ID;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.JdbcEventLogRepository.SQL_FIND_LATEST_SEQUENCE_ID;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.JdbcEventLogRepository.SQL_INSERT_EVENT_LOG;

@RunWith(MockitoJUnitRunner.class)
public class EventLogRepositoryJdbcTest {

    private static final Long LATEST_VERSION = 10L;
    private static final String TEST_APP_NAME = "TestApp";

    private static final UUID ID = UUID.randomUUID();
    private static final UUID STREAM_ID = UUID.randomUUID();
    private static final Long SEQUENCE_ID = 5L;
    private static final String NAME = "Test Name";
    private static final String PAYLOAD_JSON = "{\"field\": \"Value\"}";
    private static final String METADATA_JSON = "{\"field\": \"Value\"}";

    @Mock
    private Context initialContext;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private List<EventLog> listOfEventLogs;

    @Mock
    private Stream<EventLog> streamOfEventLogs;

    private EventLog eventLog;

    @InjectMocks
    private JdbcEventLogRepository eventLogRepositoryJdbcDelete;

    @Test
    public void shouldInsertEventLog() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL_INSERT_EVENT_LOG)).thenReturn(preparedStatement);
        eventLog = createEventLogWithSequence(SEQUENCE_ID);

        eventLogRepositoryJdbcDelete.insert(eventLog);

        verify(preparedStatement).setObject(1, eventLog.getId());
        verify(preparedStatement).setObject(2, eventLog.getStreamId());
        verify(preparedStatement).setLong(3, eventLog.getSequenceId());
        verify(preparedStatement).setString(4, eventLog.getName());
        verify(preparedStatement).setString(5, eventLog.getMetadata());
        verify(preparedStatement).setString(6, eventLog.getPayload());
        verify(preparedStatement).executeUpdate();
    }

    @Test(expected = InvalidSequenceIdException.class)
    public void shouldThrowExceptionOnNullSequenceId() throws Exception {
        eventLogRepositoryJdbcDelete.insert(createEventLogWithSequence(null));
    }

    @Test(expected = EventLogRepositoryException.class)
    public void shouldThrowExceptionOnSQLException() throws Exception {
        doThrow(SQLException.class).when(dataSource).getConnection();

        eventLogRepositoryJdbcDelete.insert(createEventLogWithSequence(SEQUENCE_ID));
    }

    @Test(expected = EventLogRepositoryException.class)
    public void shouldThrowExceptionOnNamingException() throws Exception {
        eventLogRepositoryJdbcDelete.datasource = null;
        doThrow(NamingException.class).when(initialContext).lookup(anyString());

        eventLogRepositoryJdbcDelete.insert(createEventLogWithSequence(SEQUENCE_ID));
    }

    @Test
    public void shouldReturnEmptyStreamOfEventLogs() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL_FIND_BY_STREAM_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Stream<EventLog> actualStream = eventLogRepositoryJdbcDelete.findByStreamIdOrderBySequenceIdAsc(STREAM_ID);

        verify(preparedStatement).setObject(1, STREAM_ID);
        assertThat(actualStream.count(), equalTo(0L));
    }

    @Test
    public void shouldReturnStreamOfEventLogs() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL_FIND_BY_STREAM_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        mockResultSet();

        Stream<EventLog> actualStream = eventLogRepositoryJdbcDelete.findByStreamIdOrderBySequenceIdAsc(STREAM_ID);

        verifyRead(actualStream.collect(Collectors.toList()));
        verify(preparedStatement).setObject(1, STREAM_ID);
    }

    @Test
    public void shouldReturnStreamOfEventLogsFromSequenceId() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL_FIND_BY_STREAM_ID_AND_SEQUENCE_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        mockResultSet();

        Stream<EventLog> actualStream = eventLogRepositoryJdbcDelete.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(STREAM_ID, SEQUENCE_ID);

        verifyRead(actualStream.collect(Collectors.toList()));
        verify(preparedStatement).setObject(1, STREAM_ID);
        verify(preparedStatement).setLong(2, SEQUENCE_ID);
    }

    private void verifyRead(List<EventLog> actualListOfEventLogs) {
        EventLog actualEventLog = actualListOfEventLogs.get(0);

        assertThat(actualListOfEventLogs, IsCollectionWithSize.hasSize(1));
        assertThat(actualEventLog.getId(), equalTo(ID));
        assertThat(actualEventLog.getStreamId(), equalTo(STREAM_ID));
        assertThat(actualEventLog.getSequenceId(), equalTo(SEQUENCE_ID));
        assertThat(actualEventLog.getName(), equalTo(NAME));
        assertThat(actualEventLog.getMetadata(), equalTo(METADATA_JSON));
        assertThat(actualEventLog.getPayload(), equalTo(PAYLOAD_JSON));
    }

    @Test(expected = EventLogRepositoryException.class)
    public void shouldThrowExceptionOnSQLExceptionInFindByStreamId() throws Exception {
        doThrow(SQLException.class).when(dataSource).getConnection();

        eventLogRepositoryJdbcDelete.findByStreamIdOrderBySequenceIdAsc(STREAM_ID);
    }

    @Test(expected = EventLogRepositoryException.class)
    public void shouldThrowExceptionOnNamingExceptionInFindByStreamId() throws Exception {
        eventLogRepositoryJdbcDelete.datasource = null;
        doThrow(NamingException.class).when(initialContext).lookup(anyString());

        eventLogRepositoryJdbcDelete.findByStreamIdOrderBySequenceIdAsc(STREAM_ID);
    }

    @Test(expected = EventLogRepositoryException.class)
    public void shouldThrowExceptionOnSQLExceptionInFindByStreamIdAndSequenceId() throws Exception {
        doThrow(SQLException.class).when(dataSource).getConnection();

        eventLogRepositoryJdbcDelete.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(STREAM_ID, SEQUENCE_ID);
    }

    @Test(expected = EventLogRepositoryException.class)
    public void shouldThrowExceptionOnNamingExceptionInFindByStreamIdAndSequenceId() throws Exception {
        eventLogRepositoryJdbcDelete.datasource = null;
        doThrow(NamingException.class).when(initialContext).lookup(anyString());

        eventLogRepositoryJdbcDelete.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(STREAM_ID, SEQUENCE_ID);
    }

    @Test(expected = EventLogRepositoryException.class)
    public void shouldThrowExceptionOnSQLExceptionInFindByStreamIdFromSequenceId() throws Exception {
        doThrow(SQLException.class).when(dataSource).getConnection();

        eventLogRepositoryJdbcDelete.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(STREAM_ID, SEQUENCE_ID);
    }

    @Test(expected = EventLogRepositoryException.class)
    public void shouldThrowExceptionOnNamingExceptionInFindByStreamIFromSequenceId() throws Exception {
        eventLogRepositoryJdbcDelete.datasource = null;
        doThrow(NamingException.class).when(initialContext).lookup(anyString());

        eventLogRepositoryJdbcDelete.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(STREAM_ID, SEQUENCE_ID);
    }

    @Test
    public void shouldReturnLatestSequenceId() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL_FIND_LATEST_SEQUENCE_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getLong(1)).thenReturn(LATEST_VERSION);

        Long actualLatestSequenceId = eventLogRepositoryJdbcDelete.getLatestSequenceIdForStream(STREAM_ID);

        verify(preparedStatement).setObject(1, STREAM_ID);
        assertThat(actualLatestSequenceId, equalTo(LATEST_VERSION));
    }

    @Test
    public void shouldReturnInitialSequenceId() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL_FIND_LATEST_SEQUENCE_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Long actualLatestSequenceId = eventLogRepositoryJdbcDelete.getLatestSequenceIdForStream(STREAM_ID);

        verify(preparedStatement).setObject(1, STREAM_ID);
        assertThat(actualLatestSequenceId, equalTo(JdbcEventLogRepository.INITIAL_VERSION));
    }

    @Test(expected = EventLogRepositoryException.class)
    public void shouldThrowExceptionOnSQLExceptionInGetLatestSequenceId() throws Exception {
        doThrow(SQLException.class).when(dataSource).getConnection();

        eventLogRepositoryJdbcDelete.getLatestSequenceIdForStream(STREAM_ID);
    }

    @Test(expected = EventLogRepositoryException.class)
    public void shouldThrowExceptionOnNamingExceptionInGetLatestSequenceId() throws Exception {
        eventLogRepositoryJdbcDelete.datasource = null;
        doThrow(NamingException.class).when(initialContext).lookup(anyString());

        eventLogRepositoryJdbcDelete.getLatestSequenceIdForStream(STREAM_ID);
    }

    @Test(expected = EventLogRepositoryException.class)
    public void shouldThrowExceptionWhenInitialiseInitialContext() throws InvalidSequenceIdException {
        eventLogRepositoryJdbcDelete.datasource = null;
        eventLogRepositoryJdbcDelete.initialContext = null;

        eventLog = createEventLogWithSequence(SEQUENCE_ID);

        eventLogRepositoryJdbcDelete.insert(eventLog);
    }

    @Test
    public void shouldCreateDataSource() throws Exception {
        eventLogRepositoryJdbcDelete.datasource = null;
        when(initialContext.lookup(JNDI_APP_NAME_LOOKUP)).thenReturn(TEST_APP_NAME);
        when(initialContext.lookup(String.format(JNDI_DS_EVENT_STORE_PATTERN, TEST_APP_NAME))).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL_INSERT_EVENT_LOG)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        eventLog = createEventLogWithSequence(SEQUENCE_ID);

        eventLogRepositoryJdbcDelete.insert(eventLog);
    }

    private EventLog createEventLog(UUID id, String name, UUID streamId, Long sequenceId, String payloadJSON, String metadataJSON) {
        return new EventLog(id, streamId, sequenceId, name, metadataJSON, payloadJSON);
    }

    private EventLog createEventLogWithSequence(Long sequenceId) {
        return createEventLog(UUID.randomUUID(), NAME, STREAM_ID, sequenceId, PAYLOAD_JSON, METADATA_JSON);
    }

    private void mockResultSet() throws SQLException {
        when(resultSet.getObject(PRIMARY_KEY_ID)).thenReturn(ID);
        when(resultSet.getObject(COL_STREAM_ID)).thenReturn(STREAM_ID);
        when(resultSet.getLong(COL_SEQUENCE_ID)).thenReturn(SEQUENCE_ID);
        when(resultSet.getString(COL_NAME)).thenReturn(NAME);
        when(resultSet.getString(COL_METADATA)).thenReturn(METADATA_JSON);
        when(resultSet.getString(COL_PAYLOAD)).thenReturn(PAYLOAD_JSON);
    }

}
