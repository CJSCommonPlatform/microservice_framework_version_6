package uk.gov.justice.services.event.buffer.notification;

import java.util.concurrent.BlockingQueue;

import com.impossibl.postgres.jdbc.PGDataSource;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class PostgresBlockingNotificationListenerTest {

    @Test
    public void shouldName() throws Exception {

        final PGDataSource dataSource = new PGDataSource();
        dataSource.setHost("localhost");
        dataSource.setPort(5432);
        dataSource.setDatabase("frameworkviewstore");
        dataSource.setUser("framework");
        dataSource.setPassword("framework");

        try (final PostgresBlockingNotificationListener postgresBlockingNotificationListener = new PostgresBlockingNotificationListener().startListening(dataSource)) {

            final BlockingQueue<String> queue = postgresBlockingNotificationListener.getQueue();
            while (true) {
                final String notification = queue.take();
                System.out.println(notification);
            }
        }
    }
}
