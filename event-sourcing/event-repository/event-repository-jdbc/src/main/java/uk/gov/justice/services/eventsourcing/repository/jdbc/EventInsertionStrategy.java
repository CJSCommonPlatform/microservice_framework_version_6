package uk.gov.justice.services.eventsourcing.repository.jdbc;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidPositionException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.SQLException;

public interface EventInsertionStrategy {

    /**
     * The Insert Statement as a String for the this Insertion Strategy
     *
     * @return the insert statement
     */
    String insertStatement();

    void insert(final PreparedStatementWrapper ps, final Event event) throws SQLException, InvalidPositionException;
}
