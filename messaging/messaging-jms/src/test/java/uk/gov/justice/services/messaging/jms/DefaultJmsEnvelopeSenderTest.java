package uk.gov.justice.services.messaging.jms;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJmsEnvelopeSenderTest {

    @Mock
    private EnvelopeSenderSelector envelopeSenderSelector;


    @InjectMocks
    private DefaultJmsEnvelopeSender jmsEnvelopeSender;

    @Test
    public void shouldPublishValidEnvelopeToDestination() throws Exception {

        final String destinationName = "destination name";

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final EnvelopeSender envelopeSender = mock(EnvelopeSender.class);

        when(envelopeSenderSelector.getEnvelopeSender()).thenReturn(envelopeSender);

        jmsEnvelopeSender.send(jsonEnvelope, destinationName);

        verify(envelopeSender).send(jsonEnvelope, destinationName);
    }

}
