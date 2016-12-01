package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static java.lang.String.format;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.SQLException;

public abstract class BaseEventLogInsertStrategy implements EventLogInsertionStrategy {

    static final String SQL_INSERT_EVENT_LOG = "INSERT INTO event_log (id, stream_id, sequence_id, name, metadata, payload, date_created) " +
            "VALUES(?, ?, ?, ?, ?, ?, ?)";

    /**
     * Executes the Insertion into Event Log.
     *
     * @param preparedStatement the prepared statement wrapper to use for executing the update
     * @param eventLog          the information to set into the prepared statement
     * @return the value returned from executing the update
     * @throws SQLException               if thrown by the execute update
     * @throws InvalidSequenceIdException if the version already exists or is null
     */
    protected int executeStatement(final PreparedStatementWrapper preparedStatement, final EventLog eventLog) throws SQLException, InvalidSequenceIdException {
        if (eventLog.getSequenceId() == null) {
            throw new InvalidSequenceIdException(format("Version is null for stream %s", eventLog.getStreamId()));
        }

        preparedStatement.setObject(1, eventLog.getId());
        preparedStatement.setObject(2, eventLog.getStreamId());
        preparedStatement.setLong(3, eventLog.getSequenceId());
        preparedStatement.setString(4, eventLog.getName());
        preparedStatement.setString(5, eventLog.getMetadata());
        preparedStatement.setString(6, eventLog.getPayload());
        preparedStatement.setTimestamp(7, toSqlTimestamp(eventLog.getCreatedAt()));
        return preparedStatement.executeUpdate();
    }
}
