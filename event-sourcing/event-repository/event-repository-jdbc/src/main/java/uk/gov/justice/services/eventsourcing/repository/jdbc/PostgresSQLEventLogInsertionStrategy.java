package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidPositionException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.OptimisticLockingRetryException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.SQLException;

import javax.enterprise.inject.Alternative;

@Alternative
public class PostgresSQLEventLogInsertionStrategy extends BaseEventInsertStrategy {

    static final String SQL_INSERT_EVENT = "WITH seqWrapper AS (\n" +
            "\tSELECT currval('event_log_seq') AS seqId\n" +
            ")" +
            "INSERT INTO event_log (id, stream_id, name, payload, metadata, date_created) " +
            "VALUES(?, ?, ?, ?," +

            "jsonb_set(TO_JSONB(?::JSON), '{stream, sequence_number}', TO_JSONB( (SELECT seqId FROM seqWrapper) ), true), " +
            "?);";
    @Override
    public String insertStatement() {return SQL_INSERT_EVENT + " ON CONFLICT DO NOTHING";}

    /**
     * Tries to insert the given event into the event log. If database is PostgresSQL and
     * version&gt;=9.5. Uses PostgreSQl-specific sql clause.
     *
     * @param event the event to insert
     * @throws SQLException               if thrown from {@link BaseEventInsertStrategy#executeStatement}
     * @throws InvalidPositionException if the version already exists or is null.
     */
    @Override
    public void insert(final PreparedStatementWrapper ps, final Event event) throws SQLException, InvalidPositionException {
        final int updatedRows = executeStatement(ps, event);

        if (updatedRows == 0) {
            throw new OptimisticLockingRetryException(format("Locking Exception while storing sequence %s of stream %s",
                    event.getSequenceId(), event.getStreamId()));
        }
    }
}
