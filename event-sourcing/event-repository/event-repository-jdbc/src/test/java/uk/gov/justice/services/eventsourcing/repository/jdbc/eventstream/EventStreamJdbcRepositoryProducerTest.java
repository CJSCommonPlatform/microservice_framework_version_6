package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

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
public class EventStreamJdbcRepositoryProducerTest {

    @Mock
    private UnmanagedBeanCreator unmanagedBeanCreator;

    @InjectMocks
    private EventStreamJdbcRepositoryProducer eventStreamJdbcRepositoryProducer;

    @Test
    public void shouldProduceEventStreamManager() {
        final EventStreamJdbcRepository eventStreamJdbcRepository = mock(EventStreamJdbcRepository.class);

        when(unmanagedBeanCreator.create(EventStreamJdbcRepository.class)).thenReturn(eventStreamJdbcRepository);

        final EventStreamJdbcRepository actualEventStreamJdbcRepository = eventStreamJdbcRepositoryProducer.eventStreamJdbcRepository();

        assertThat(actualEventStreamJdbcRepository, is(eventStreamJdbcRepository));
    }
}