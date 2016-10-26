package uk.gov.justice.repository;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.jdbc.snapshot.jdbc.snapshot.SnapshotJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.naming.NamingException;

public class SnapshotOpenEjbAwareJdbcRepository extends SnapshotJdbcRepository {

    static final String SQL_SNAPSHOT_COUNT_BY_STREAM_ID = "SELECT count(*) FROM snapshot WHERE stream_id=? ";

    @Override
    protected String jndiName() {
        return "java:openejb/Resource/eventStore";
    }

    public long snapshotCount(final UUID streamId) {

        try (final Connection connection = getDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(SQL_SNAPSHOT_COUNT_BY_STREAM_ID)) {
            preparedStatement.setObject(1, streamId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format(READING_STREAM_EXCEPTION, streamId), e);
        }
    }
}
