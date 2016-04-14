package uk.gov.justice.services.eventsourcing.publisher.jms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import javax.jms.Destination;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JmsEventPublisherTest {

    private static final String NAME = "test.event.listener";

    @Mock
    private JmsEnvelopeSender jmsEnvelopeSender;

    @Mock
    private MessagingDestinationResolver messagingDestinationResolver;

    @Mock
    private Destination destination;

    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private Metadata metadata;

    @Test
    public void shouldPublishEnvelope() {
        JmsEventPublisher jmsEventPublisher = new JmsEventPublisher();
        jmsEventPublisher.jmsEnvelopeSender = jmsEnvelopeSender;
        jmsEventPublisher.messagingDestinationResolver = messagingDestinationResolver;
        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(NAME);
        when(messagingDestinationResolver.resolve(NAME)).thenReturn(destination);

        jmsEventPublisher.publish(jsonEnvelope);

        verify(jmsEnvelopeSender).send(jsonEnvelope, destination);
    }

}