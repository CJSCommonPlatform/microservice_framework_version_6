package uk.gov.justice.services.eventsourcing.publishing;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishing.helpers.EventFactory;
import uk.gov.justice.services.eventsourcing.publishing.helpers.EventStoreInitializer;
import uk.gov.justice.services.eventsourcing.publishing.helpers.TestEventInserter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventDeQueuerIT {

    private final DataSource dataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
    private final TestEventInserter testEventInserter = new TestEventInserter();
    private final EventFactory eventFactory = new EventFactory();

    @Mock
    private SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    @InjectMocks
    private EventDeQueuer eventDeQueuer;

    @Before
    public void initDatabase() throws Exception {
        new EventStoreInitializer().initializeEventStore(dataSource);
    }

    @Test
    public void shouldPopEventsFromThePublishQueue() throws Exception {

        when(subscriptionDataSourceProvider.getEventStoreDataSource()).thenReturn(dataSource);

        assertThat(eventDeQueuer.popNextEvent().isPresent(), is(false));

        final Event event_1 = eventFactory.createEvent("example.first-event", 1L);
        final Event event_2 = eventFactory.createEvent("example.second-event", 2L);
        final Event event_3 = eventFactory.createEvent("example.third-event", 3L);

        testEventInserter.insertIntoEventLog(event_1);
        testEventInserter.insertIntoEventLog(event_2);
        testEventInserter.insertIntoEventLog(event_3);

        assertThat(eventDeQueuer.popNextEvent().get(), is(event_1));
        assertThat(eventDeQueuer.popNextEvent().get(), is(event_2));
        assertThat(eventDeQueuer.popNextEvent().get(), is(event_3));

        assertThat(eventDeQueuer.popNextEvent().isPresent(), is(false));
    }
}
