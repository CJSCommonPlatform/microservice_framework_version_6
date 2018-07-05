package uk.gov.justice.services.event.buffer.notification;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

public class PostgresNonBlockingNotificationListener implements AutoCloseable {

    private static final String NOTIFICATION_QUEUE_NAME = "postgres_notification_queue";

    private Connection connection;

    public PostgresNonBlockingNotificationListener startListening(final DataSource dataSource) throws SQLException {

        connection = dataSource.getConnection();

        final Statement stmt = connection.createStatement();
        stmt.execute("LISTEN " + NOTIFICATION_QUEUE_NAME);
        stmt.close();

        return this;
    }

    public List<PGNotification> getNotifications() throws SQLException {

        final Statement statement = connection.createStatement();
        final ResultSet resultSet = statement.executeQuery("SELECT 1");
        resultSet.close();
        statement.close();

        final PGConnection pgConnection = (PGConnection) connection;
        final PGNotification[] pgNotifications = pgConnection.getNotifications();

        if (pgNotifications == null) {
            return emptyList();
        }

        return asList(pgNotifications);
    }

    @Override
    public void close() throws Exception {
        closeQuietly(connection);
    }

    private void closeQuietly(final Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (final Exception ignored) {
        }
    }
}
