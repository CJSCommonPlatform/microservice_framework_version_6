package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.BaseEventLogInsertStrategy.SQL_INSERT_EVENT_LOG;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.core.exception.OptimisticLockingRetryException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PostgresSQLEventLogInsertionStrategyTest {

    private static final int INSERTED = 1;
    private static final int CONFLICT_OCCURRED = 0;

    private static final UUID ID = UUID.randomUUID();
    private static final UUID STREAM_ID = UUID.randomUUID();
    private static final long SEQUENCE_ID = 1L;
    private static final String NAME = "Name";
    private static final String METADATA = "metadata";
    private static final String PAYLOAD = "payload";
    final ZonedDateTime createdAt = new UtcClock().now();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private EventLog eventLog;

    @Mock
    private PreparedStatementWrapper preparedStatement;

    @InjectMocks
    private PostgresSQLEventLogInsertionStrategy strategy;

    @Test
    public void shouldExecutePreparedStatementAndCompleteIfRowIsInserted() throws Exception {
        when(eventLog.getId()).thenReturn(ID);
        when(eventLog.getStreamId()).thenReturn(STREAM_ID);
        when(eventLog.getSequenceId()).thenReturn(SEQUENCE_ID);
        when(eventLog.getName()).thenReturn(NAME);
        when(eventLog.getMetadata()).thenReturn(METADATA);
        when(eventLog.getPayload()).thenReturn(PAYLOAD);
        when(preparedStatement.executeUpdate()).thenReturn(INSERTED);
        when(eventLog.getCreatedAt()).thenReturn(createdAt);

        strategy.insert(preparedStatement, eventLog);

        verify(preparedStatement).setObject(1, ID);
        verify(preparedStatement).setObject(2, STREAM_ID);
        verify(preparedStatement).setLong(3, SEQUENCE_ID);
        verify(preparedStatement).setString(4, NAME);
        verify(preparedStatement).setString(5, METADATA);
        verify(preparedStatement).setString(6, PAYLOAD);
        verify(preparedStatement).setTimestamp(7, toSqlTimestamp(createdAt));
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void shouldExecutePreparedStatementAndThrowExceptionIfRowWasNotInsertedDueToConflict() throws Exception {
        when(eventLog.getId()).thenReturn(ID);
        when(eventLog.getStreamId()).thenReturn(STREAM_ID);
        when(eventLog.getSequenceId()).thenReturn(SEQUENCE_ID);
        when(eventLog.getName()).thenReturn(NAME);
        when(eventLog.getMetadata()).thenReturn(METADATA);
        when(eventLog.getPayload()).thenReturn(PAYLOAD);
        when(preparedStatement.executeUpdate()).thenReturn(CONFLICT_OCCURRED);
        when(eventLog.getCreatedAt()).thenReturn(createdAt);

        expectedException.expect(OptimisticLockingRetryException.class);
        expectedException.expectMessage("Locking Exception while storing sequence 1 of stream");

        strategy.insert(preparedStatement, eventLog);

        verify(preparedStatement).setObject(1, ID);
        verify(preparedStatement).setObject(2, SEQUENCE_ID);
        verify(preparedStatement).setLong(3, SEQUENCE_ID);
        verify(preparedStatement).setString(4, NAME);
        verify(preparedStatement).setString(5, METADATA);
        verify(preparedStatement).setString(6, PAYLOAD);
        verify(preparedStatement).setTimestamp(7, toSqlTimestamp(createdAt));
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void shouldReturnTheDefaultSqlInsertStatementWithPostgresDoNothingSuffix() throws Exception {
        assertThat(strategy.insertStatement(), is(SQL_INSERT_EVENT_LOG + " ON CONFLICT DO NOTHING"));
    }
}