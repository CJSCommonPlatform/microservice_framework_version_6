package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.BaseEventInsertStrategy.SQL_INSERT_EVENT;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AnsiSQLEventInsertionStrategyTest {

    @Mock
    private Event event;

    @Mock
    private PreparedStatementWrapper preparedStatement;

    @InjectMocks
    private AnsiSQLEventLogInsertionStrategy strategy;

    @Test
    public void shouldExecutePreparedStatement() throws Exception {
        final UUID id = UUID.randomUUID();
        final UUID streamId = UUID.randomUUID();
        final long sequenceId = 1L;
        final String name = "Name";
        final String metadata = "metadata";
        final String payload = "payload";
        final ZonedDateTime createdAt = new UtcClock().now();

        when(event.getId()).thenReturn(id);
        when(event.getStreamId()).thenReturn(streamId);
        when(event.getSequenceId()).thenReturn(sequenceId);
        when(event.getName()).thenReturn(name);
        when(event.getMetadata()).thenReturn(metadata);
        when(event.getPayload()).thenReturn(payload);
        when(event.getCreatedAt()).thenReturn(createdAt);

        strategy.insert(preparedStatement, event);

        verify(preparedStatement).setObject(1, id);
        verify(preparedStatement).setObject(2, streamId);
        verify(preparedStatement).setLong(3, sequenceId);
        verify(preparedStatement).setString(4, name);
        verify(preparedStatement).setString(5, metadata);
        verify(preparedStatement).setString(6, payload);
        verify(preparedStatement).setTimestamp(7, toSqlTimestamp(createdAt));
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void shouldReturnTheDefaultSqlInsertStatement() throws Exception {
        assertThat(strategy.insertStatement(), is(SQL_INSERT_EVENT));
    }
}