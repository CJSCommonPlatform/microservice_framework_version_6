package uk.gov.justice.services.core.sender;

import com.google.common.testing.EqualsTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.jms.JmsEndpoints;
import uk.gov.justice.services.core.jms.JmsSender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

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

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    @Test
    public void shouldTestEqualsAndHashCode() {
        JmsEndpoints jmsEndpoints = new JmsEndpoints();
        JmsSender jmsSender = new JmsSender();

        final Sender item1 = createSender(jmsSender, COMMAND_API, jmsEndpoints);
        final Sender item2 = createSender(jmsSender, COMMAND_API, jmsEndpoints);
        final Sender item3 = createSender(null, COMMAND_API, jmsEndpoints);
        final Sender item4 = createSender(jmsSender, COMMAND_HANDLER, jmsEndpoints);
        final Sender item5 = createSender(jmsSender, COMMAND_API, null);

        new EqualsTester()
                .addEqualityGroup(item1, item2)
                .addEqualityGroup(item3)
                .addEqualityGroup(item4)
                .addEqualityGroup(item5)
                .testEquals();
    }

    @Test
    public void shouldSendEnvelopeToEndpoint() throws Exception {
        DefaultSender sender = new DefaultSender(jmsSender, COMMAND_CONTROLLER, new JmsEndpoints());
        sender.send(envelope);
        verify(jmsSender).send(ENDPOINT, envelope);
    }

    private Sender createSender(final JmsSender jmsSender, final Component destinationComponent, final JmsEndpoints jmsEndpoints) {
        return new DefaultSender(jmsSender, destinationComponent, jmsEndpoints);
    }
}