package uk.gov.justice.services.event.buffer.notification;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;
import com.impossibl.postgres.jdbc.PGDataSource;

public class PostgresBlockingNotificationListener implements Closeable {

    private static final String NOTIFICATION_QUEUE_NAME = "postgres_notification_queue";

    final BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

    private PGConnection connection;

    public PostgresBlockingNotificationListener startListening(final PGDataSource dataSource) {

        final PGNotificationListener listener = (
                final int processId,
                final String channelName,
                final String payload
        ) -> queue.add(payload);

        Statement statement = null;
        try {
            connection = (PGConnection) dataSource.getConnection();
            statement = connection.createStatement();
            statement.execute("LISTEN " + NOTIFICATION_QUEUE_NAME);
        } catch (final SQLException e) {
            closeQuietly(connection);
            throw new RuntimeException("Failed to listen to postgres notification queue", e);
        } finally {
            closeQuietly(statement);
        }

        connection.addNotificationListener(listener);

        return this;
    }

    public BlockingQueue<String> getQueue() {
        return queue;
    }

    @Override
    public void close() throws IOException {
        closeQuietly(connection);
    }

    private void closeQuietly(final Statement statement) {

        if (statement == null) {
            return;
        }

        try {
            statement.close();
        } catch (final SQLException ignored) {
        }
    }

    private void closeQuietly(final Connection connection) {

        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (final SQLException ignored) {
        }
    }
}
