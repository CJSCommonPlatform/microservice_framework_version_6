package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.OptimisticLockingRetryException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.SQLException;

import javax.enterprise.inject.Alternative;

@Alternative
public class PostgresSQLEventLogInsertionStrategy extends BaseEventLogInsertStrategy {

    @Override
    public String insertStatement() {
        return SQL_INSERT_EVENT_LOG + " ON CONFLICT DO NOTHING";
    }

    /**
     * Tries to insert the given event into the event log. If database is PostgresSQL and
     * version&gt;=9.5. Uses PostgreSQl-specific sql clause.
     *
     * @param eventLog the event to insert
     * @throws SQLException               if thrown from {@link BaseEventLogInsertStrategy#executeStatement}
     * @throws InvalidSequenceIdException if the version already exists or is null.
     */
    @Override
    public void insert(final PreparedStatementWrapper ps, final EventLog eventLog) throws SQLException, InvalidSequenceIdException {
        final int updatedRows = executeStatement(ps, eventLog);

        if (updatedRows == 0) {
            throw new OptimisticLockingRetryException(format("Locking Exception while storing sequence %s of stream %s",
                    eventLog.getSequenceId(), eventLog.getStreamId()));
        }
    }
}
