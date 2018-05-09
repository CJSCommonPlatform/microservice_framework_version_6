package uk.gov.justice.services.eventsourcing.source.core;

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
public class EventStreamManagerProducerTest {

    @Mock
    private UnmanagedBeanCreator unmanagedBeanCreator;

    @InjectMocks
    private EventStreamManagerProducer eventStreamManagerProducer;

    @Test
    public void shouldProduceEventStreamManager() {
        final EventStreamManager eventStreamManager = mock(EventStreamManager.class);

        when(unmanagedBeanCreator.create(EventStreamManager.class)).thenReturn(eventStreamManager);

        final EventStreamManager actualEventStreamManager = eventStreamManagerProducer.eventStreamManager();

        assertThat(actualEventStreamManager, is(eventStreamManager));
    }
}