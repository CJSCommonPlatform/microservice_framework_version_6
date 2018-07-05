package uk.gov.justice.services.eventsourcing.publishing;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publisher.jms.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class EventDeQueuerAndPublisherTest {

    @Mock
    private EventDeQueuer eventDeQueuer;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private EventConverter eventConverter;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventDeQueuerAndPublisher eventDeQueuerAndPublisher;

    @Test
    public void shouldPublishEventIfFound() throws Exception {

        final String eventName = "event-name";

        final Event event = mock(Event.class);
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        when(eventDeQueuer.popNextEvent()).thenReturn(of(event));
        when(eventConverter.envelopeOf(event)).thenReturn(jsonEnvelope);
        when(event.getName()).thenReturn(eventName);

        assertThat(eventDeQueuerAndPublisher.deQueueAndPublish(), is(true));

        verify(logger).debug("Publishing event {}", eventName);
        verify(eventPublisher).publish(jsonEnvelope);
    }

    @Test
    public void shouldNotPublishIfNoEventsAreFoundOnQueue() throws Exception {

        when(eventDeQueuer.popNextEvent()).thenReturn(empty());

        assertThat(eventDeQueuerAndPublisher.deQueueAndPublish(), is(false));

        //verify(logger).debug("No event found to publish");

        verifyZeroInteractions(eventConverter);
        verifyZeroInteractions(eventPublisher);
    }
}
