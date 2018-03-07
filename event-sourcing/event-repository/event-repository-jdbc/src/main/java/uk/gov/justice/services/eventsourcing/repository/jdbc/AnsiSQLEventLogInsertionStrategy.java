package uk.gov.justice.services.eventsourcing.repository.jdbc;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidPositionException;
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
     * @throws InvalidPositionException if an event already exists at the specified position.
     */
    @Override
    public void insert(final PreparedStatementWrapper ps, final Event event) throws SQLException, InvalidPositionException {
        executeStatement(ps, event);
    }
}
