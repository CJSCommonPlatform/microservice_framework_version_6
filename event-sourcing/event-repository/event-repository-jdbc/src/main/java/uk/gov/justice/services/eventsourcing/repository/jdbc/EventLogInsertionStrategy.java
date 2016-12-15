package uk.gov.justice.services.eventsourcing.repository.jdbc;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.SQLException;

public interface EventLogInsertionStrategy {

    /**
     * The Insert Statement as a String for the this Insertion Strategy
     *
     * @return the insert statement
     */
    String insertStatement();

    void insert(final PreparedStatementWrapper ps, final EventLog eventLog) throws SQLException, InvalidSequenceIdException;
}
