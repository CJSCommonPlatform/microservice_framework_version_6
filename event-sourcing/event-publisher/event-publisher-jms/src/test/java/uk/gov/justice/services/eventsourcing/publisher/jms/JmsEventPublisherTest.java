package uk.gov.justice.services.eventsourcing.publisher.jms;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.builder.JsonEnvelopeBuilder.envelope;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JmsEventPublisherTest {

    @Mock
    private JmsEnvelopeSender jmsEnvelopeSender;

    @Mock
    private EventDestinationResolver eventDestinationResolver;


    @Test
    public void shouldPublishEnvelope() {
        JmsEventPublisher jmsEventPublisher = new JmsEventPublisher();
        jmsEventPublisher.jmsEnvelopeSender = jmsEnvelopeSender;
        jmsEventPublisher.eventDestinationResolver = eventDestinationResolver;

        final String eventName = "test.event.listener";

        final JsonEnvelope envelope = envelope().withMetadataOf("id", UUID.randomUUID().toString(), "name", eventName).build();
        final String destinationName = "someName";
        when(eventDestinationResolver.destinationNameOf(eventName)).thenReturn(destinationName);

        jmsEventPublisher.publish(envelope);

        verify(jmsEnvelopeSender).send(envelope, destinationName);
    }

}
