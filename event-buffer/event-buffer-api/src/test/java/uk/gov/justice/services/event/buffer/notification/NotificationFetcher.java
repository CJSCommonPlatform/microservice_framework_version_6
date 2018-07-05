package uk.gov.justice.services.event.buffer.notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class NotificationFetcher {

    private final DataSource dataSource;

    public NotificationFetcher(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Notification> getNotifications() throws SQLException {

        final List<Notification> notifications = new ArrayList<>();

        try (final Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT table_name, action, data FROM notification_log FOR UPDATE SKIP LOCKED")) {
                try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        final String tableName = resultSet.getString("table_name");
                        final String action = resultSet.getString("action");
                        final String data = resultSet.getString("data");

                        notifications.add(new Notification(tableName, action, data));


                        final String deleteSql = "DELETE from notification_log WHERE table_name = ? AND action = ? AND data = ?";
                        try (final PreparedStatement deletePreparedStatement = connection.prepareStatement(deleteSql)) {

                            deletePreparedStatement.setString(1, tableName);
                            deletePreparedStatement.setString(2, action);
                            deletePreparedStatement.setString(3, data);

                            deletePreparedStatement.executeUpdate();
                        }
                    }
                }
            } finally {
                connection.commit();
            }

        }
        
        return notifications;
    }
}
