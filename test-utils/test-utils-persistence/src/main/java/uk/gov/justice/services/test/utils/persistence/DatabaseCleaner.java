package uk.gov.justice.services.test.utils.persistence;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

import uk.gov.justice.services.jdbc.persistence.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;

public class DatabaseCleaner {

    private static final String SQL_PATTERN = "DELETE FROM %s";

    private final TestJdbcConnectinProvider testJdbcConnectinProvider;

    public DatabaseCleaner() {
        this(new TestJdbcConnectinProvider());
    }

    @VisibleForTesting
    DatabaseCleaner(final TestJdbcConnectinProvider testJdbcConnectinProvider) {
        this.testJdbcConnectinProvider = testJdbcConnectinProvider;
    }

    public void cleanStreamBufferTable(final String contextName) {
        cleanViewStoreTables(contextName, singletonList("stream_buffer"));
    }

    public void cleanStreamStatusTable(final String contextName) {
        cleanViewStoreTables(contextName, singletonList("stream_status"));
    }

    public void cleanEventLogTable(final String contextName) {

        try (final Connection connection = testJdbcConnectinProvider.getEventStoreConnection(contextName)) {

            cleanTable("event_log", connection);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to commit or close database connection", e);
        }
    }

    public void cleanViewStoreTables(final String contextName, final List<String> tableNames) {

        try (final Connection connection = testJdbcConnectinProvider.getViewStoreConnection(contextName)) {
            for (String tableName : tableNames) {
                cleanTable(tableName, connection);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to commit or close database connection", e);
        }
    }

    private void cleanTable(final String tableName, final Connection connection) {

        final String sql = format(SQL_PATTERN, tableName);
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete content from table " + tableName, e);
        }
    }
}
