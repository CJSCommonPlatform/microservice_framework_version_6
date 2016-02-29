package uk.gov.justice.services.core.sender;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.jms.JmsEndpoints;
import uk.gov.justice.services.core.jms.JmsSender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSenderTest {

    private static final String NAME = "context.commands.command-name";
    private static final String ENDPOINT = "context.controller.commands";
    private static final String CONTEXT_NAME = "context";

    @Mock
    private Envelope envelope;

    @Mock
    private Metadata metadata;

    @Mock
    private JmsSender jmsSender;

    @Before
    public void setup() {
        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(NAME);
        doNothing().when(jmsSender).send(new JmsEndpoints().getEndpoint(COMMAND_CONTROLLER, CONTEXT_NAME), envelope);
    }

    @Test
    public void shouldSendEnvelopeToEndpoint() throws Exception {
        DefaultSender sender = new DefaultSender(jmsSender, COMMAND_CONTROLLER, new JmsEndpoints());
        sender.send(envelope);
        verify(jmsSender).send(ENDPOINT, envelope);
    }
}