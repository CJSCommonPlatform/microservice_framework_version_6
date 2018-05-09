package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

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
public class EventJdbcRepositoryProducerTest {

    @Mock
    private UnmanagedBeanCreator unmanagedBeanCreator;

    @InjectMocks
    private EventJdbcRepositoryProducer eventJdbcRepositoryProducer;

    @Test
    public void shouldProduceEventStreamManager() {
        final EventJdbcRepository eventJdbcRepository = mock(EventJdbcRepository.class);

        when(unmanagedBeanCreator.create(EventJdbcRepository.class)).thenReturn(eventJdbcRepository);

        final EventJdbcRepository actualEventJdbcRepository = eventJdbcRepositoryProducer.eventJdbcRepository();

        assertThat(actualEventJdbcRepository, is(eventJdbcRepository));
    }
}