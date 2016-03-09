package uk.gov.justice.services.eventsourcing.publisher.jms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import javax.jms.Destination;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JmsEventPublisherTest {

    private static final String NAME = "test.events.listener";

    @Mock
    private JmsEnvelopeSender jmsEnvelopeSender;

    @Mock
    private MessagingDestinationResolver messagingDestinationResolver;

    @Mock
    private Destination destination;

    @Mock
    private Envelope envelope;

    @Mock
    private Metadata metadata;

    @Test
    public void shouldPublishEnvelope() {
        JmsEventPublisher jmsEventPublisher = new JmsEventPublisher();
        jmsEventPublisher.jmsEnvelopeSender = jmsEnvelopeSender;
        jmsEventPublisher.messagingDestinationResolver = messagingDestinationResolver;
        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(NAME);
        when(messagingDestinationResolver.resolve(NAME)).thenReturn(destination);

        jmsEventPublisher.publish(envelope);

        verify(jmsEnvelopeSender).send(envelope, destination);
    }

}