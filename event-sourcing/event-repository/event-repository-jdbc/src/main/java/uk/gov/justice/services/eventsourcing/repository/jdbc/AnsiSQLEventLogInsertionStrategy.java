package uk.gov.justice.services.eventsourcing.repository.jdbc;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.SQLException;

import javax.enterprise.inject.Alternative;

@Alternative
public class AnsiSQLEventLogInsertionStrategy extends BaseEventLogInsertStrategy {

    @Override
    public String insertStatement() {
        return SQL_INSERT_EVENT_LOG;
    }

    /**
     * Insert the given event into the event log.
     *
     * @param eventLog the event to insert
     * @throws InvalidSequenceIdException if the version already exists or is null.
     */
    @Override
    public void insert(final PreparedStatementWrapper ps, final EventLog eventLog) throws SQLException, InvalidSequenceIdException {
        executeStatement(ps, eventLog);
    }
}
