package uk.gov.justice.services.repository;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class OpenEjbAwareEventJdbcRepository extends EventJdbcRepository {

    private static final String SQL_EVENT_LOG_COUNT_BY_STREAM_ID = "SELECT count(*) FROM event_log WHERE stream_id=? ";

    public int eventCount(final UUID streamId) {

        try (final Connection connection = getDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(SQL_EVENT_LOG_COUNT_BY_STREAM_ID)) {
            preparedStatement.setObject(1, streamId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception getting count of event log entries for %s", streamId), e);
        }
    }

    @Override
    protected String jndiName() {
        return "java:openejb/Resource/eventStore";
    }
}
