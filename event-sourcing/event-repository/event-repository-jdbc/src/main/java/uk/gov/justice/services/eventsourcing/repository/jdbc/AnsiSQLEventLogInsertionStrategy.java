package uk.gov.justice.services.eventsourcing.repository.jdbc;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.SQLException;

import javax.enterprise.inject.Alternative;

@Alternative
public class AnsiSQLEventLogInsertionStrategy extends BaseEventInsertStrategy {

    @Override
    public String insertStatement() {
        return SQL_INSERT_EVENT;
    }

    /**
     * Insert the given event into the event log.
     *
     * @param event the event to insert
     * @throws InvalidSequenceIdException if the version already exists or is null.
     */
    @Override
    public void insert(final PreparedStatementWrapper ps, final Event event) throws SQLException, InvalidSequenceIdException {
        executeStatement(ps, event);
    }
}
