package uk.gov.justice.services.core.jms;

import com.google.common.testing.EqualsTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.context.ContextName;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import javax.jms.Destination;
import javax.naming.NamingException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;

@RunWith(MockitoJUnitRunner.class)
public class JmsSenderTest {

    private static final String QUEUE_NAME = "test.controller.commands";
    private static final String NAME = "test.commands.do-something";
    @Mock
    JmsEnvelopeSender jmsEnvelopeSender;
    @Mock
    JmsDestinations jmsDestinations;
    @Mock
    Destination destination;
    @Mock
    private JsonEnvelope envelope;
    @Mock
    private Metadata metadata;

    @Before
    public void setup() throws Exception {

        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(NAME);

    }

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    @Test
    public void shouldTestEqualsAndHashCode() throws NamingException {
        final JmsDestinations jmsDestinations = new JmsDestinations();

        final JmsSender item1 = new JmsSender(COMMAND_API, jmsDestinations, jmsEnvelopeSender);
        final JmsSender item2 = new JmsSender(COMMAND_API, jmsDestinations, jmsEnvelopeSender);
        final JmsSender item3 = new JmsSender(COMMAND_CONTROLLER, jmsDestinations, jmsEnvelopeSender);

        new EqualsTester()
                .addEqualityGroup(item1, item2)
                .addEqualityGroup(item3)
                .testEquals();
    }

    @Test
    public void shouldSendValidEnvelopeToTheQueue() throws Exception {

        final JmsSender jmsSender = jmsSenderWithComponent(COMMAND_CONTROLLER);

        when(jmsDestinations.getDestination(COMMAND_CONTROLLER, ContextName.fromName(QUEUE_NAME))).thenReturn(destination);

        jmsSender.send(envelope);
        verify(jmsEnvelopeSender).send(envelope, destination);

    }

    private JmsSender jmsSenderWithComponent(final Component component) {
        final JmsSender jmsSender = new JmsSender(component, jmsDestinations, jmsEnvelopeSender);
        return jmsSender;
    }

}
