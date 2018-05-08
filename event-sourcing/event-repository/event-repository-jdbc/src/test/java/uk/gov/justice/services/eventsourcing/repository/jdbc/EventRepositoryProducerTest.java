package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.cdi.UnmanagedBeanCreator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventRepositoryProducerTest {

    @Mock
    private UnmanagedBeanCreator unmanagedBeanCreator;

    @InjectMocks
    private EventRepositoryProducer eventRepositoryProducer;

    @Test
    public void shouldProduceEventStreamManager() {
        final JdbcBasedEventRepository eventRepository = mock(JdbcBasedEventRepository.class);

        when(unmanagedBeanCreator.create(JdbcBasedEventRepository.class)).thenReturn(eventRepository);

        final EventRepository actualEventRepository = eventRepositoryProducer.eventRepository();

        assertThat(actualEventRepository, is(eventRepository));
    }
}