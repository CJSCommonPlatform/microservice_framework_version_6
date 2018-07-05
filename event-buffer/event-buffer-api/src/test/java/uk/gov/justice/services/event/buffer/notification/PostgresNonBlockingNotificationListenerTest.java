package uk.gov.justice.services.event.buffer.notification;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.postgresql.PGNotification;
import org.postgresql.ds.PGSimpleDataSource;

@Ignore
public class PostgresNonBlockingNotificationListenerTest {

    @Test
    public void shouldName() throws Exception {

        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setPortNumber(5432);
        dataSource.setDatabaseName("frameworkviewstore");
        dataSource.setUser("framework");
        dataSource.setPassword("framework");


        try(final PostgresNonBlockingNotificationListener postgresNonBlockingNotificationListener = new PostgresNonBlockingNotificationListener().startListening(dataSource)) {

            while(true) {
                final List<PGNotification> notifications = postgresNonBlockingNotificationListener.getNotifications();

                System.out.println("Received " + notifications.size() + " notifications");

                notifications.forEach(notification -> System.out.println(notification.getParameter()));

                Thread.sleep(500);
            }
        }
    }
}
