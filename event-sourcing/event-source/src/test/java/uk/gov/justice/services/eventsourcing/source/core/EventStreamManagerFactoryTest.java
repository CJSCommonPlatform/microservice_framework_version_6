package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;

import java.lang.reflect.Field;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamManagerFactoryTest {

    @Mock
    private PublishingEventAppender eventAppender;

    @Mock
    private Logger logger;

    @Mock
    private SystemEventService systemEventService;

    @Mock
    private Enveloper enveloper;

    @Mock
    private PublishingEventAppenderFactory publishingEventAppenderFactory;

    @InjectMocks
    private EventStreamManagerFactory eventStreamManagerFactory;

    @Test
    public void shouldProduceEventStreamManager() throws Exception {

        final long maxRetry = 23L;
        final String eventSourceName = "evenSourceName";

        eventStreamManagerFactory.maxRetry = maxRetry;

        final EventRepository eventRepository = mock(EventRepository.class);
        when(publishingEventAppenderFactory.publishingEventAppender(eventRepository)).thenReturn(eventAppender);

        final EventStreamManager eventStreamManager = eventStreamManagerFactory.eventStreamManager(
                eventRepository, eventSourceName
        );

        assertThat(getField(eventStreamManager, "eventAppender", EventAppender.class), is(eventAppender));
        assertThat(getField(eventStreamManager, "maxRetry", Long.class), is(maxRetry));
        assertThat(getField(eventStreamManager, "logger", Logger.class), is(logger));
        assertThat(getField(eventStreamManager, "systemEventService", SystemEventService.class), is(systemEventService));
        assertThat(getField(eventStreamManager, "enveloper", Enveloper.class), is(enveloper));
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(
            final EventStreamManager eventStreamManager,
            final String fieldName,
            @SuppressWarnings("unused") final Class<T> type) throws Exception {

        final Field field = eventStreamManager.getClass().getDeclaredField(fieldName);

        field.setAccessible(true);

        return (T) field.get(eventStreamManager);
    }
}
