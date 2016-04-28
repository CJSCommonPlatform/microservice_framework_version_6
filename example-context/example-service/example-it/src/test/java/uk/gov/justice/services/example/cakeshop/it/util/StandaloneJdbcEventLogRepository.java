package uk.gov.justice.services.example.cakeshop.it.util;


import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.JdbcEventLogRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.EventLogRepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

import javax.sql.DataSource;

/**
 * Standalone repository class to access event streams. To be used in integration testing
 */
public class StandaloneJdbcEventLogRepository extends JdbcEventLogRepository {
    static final String SQL_FIND_ALL = "SELECT * FROM event_log";

    private final DataSource datasource;

    public StandaloneJdbcEventLogRepository(DataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    protected DataSource getDataSource() {
        return datasource;
    }

    /**
     * @return all events
     */
    public Stream<EventLog> findAll() {

        List<EventLog> events;
        try (final Connection connection = getDataSource().getConnection();
             final PreparedStatement ps = connection.prepareStatement(SQL_FIND_ALL)) {

            events = extractResults(ps);
        } catch (SQLException e) {
            throw new EventLogRepositoryException("Error fetching event stream", e);
        }

        return events.stream();
    }

}
