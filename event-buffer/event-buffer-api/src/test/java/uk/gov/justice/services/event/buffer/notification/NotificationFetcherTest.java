package uk.gov.justice.services.event.buffer.notification;

import uk.gov.justice.services.event.buffer.api.DataSourceFactory;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Ignore;
import org.junit.Test;


@Ignore
public class NotificationFetcherTest {

    @Test
    public void shouldGetAllLatestNotifications() throws Exception {


        final DataSource dataSource = new DataSourceFactory().createDataSource();

        final NotificationFetcher notificationFetcher = new NotificationFetcher(dataSource);

        while(true) {
            final List<Notification> notifications = notificationFetcher.getNotifications();

            System.out.println("Found " + notifications.size() + " notifications");

            notifications.forEach(System.out::println);

            Thread.sleep(2000);
        }
    }
}
