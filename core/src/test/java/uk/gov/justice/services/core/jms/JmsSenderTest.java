package uk.gov.justice.services.core.jms;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.context.ContextName;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import javax.jms.Destination;
import javax.naming.NamingException;

import com.google.common.testing.EqualsTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class JmsSenderTest {

    private static final String QUEUE_NAME = "test.controller.command";

    private static final String NAME = "test.command.do-something";

    private JsonEnvelope envelope;

    @Mock
    JmsEnvelopeSender jmsEnvelopeSender;

    @Mock
    JmsDestinations jmsDestinations;

    @Mock
    SystemUserUtil systemUserUtil;

    @Mock
    Destination destination;

    @Mock
    private Logger logger;

    @Before
    public void setup() throws Exception {
        envelope = envelope().with(metadataWithRandomUUID(NAME)).build();
    }

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    @Test
    public void shouldTestEqualsAndHashCode() throws NamingException {
        final JmsDestinations jmsDestinations = new DefaultJmsDestinations();

        final JmsSender item1 = new JmsSender(COMMAND_API, jmsDestinations, jmsEnvelopeSender, logger, systemUserUtil);
        final JmsSender item2 = new JmsSender(COMMAND_API, jmsDestinations, jmsEnvelopeSender, logger, systemUserUtil);
        final JmsSender item3 = new JmsSender(COMMAND_CONTROLLER, jmsDestinations, jmsEnvelopeSender, logger, systemUserUtil);

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

        verify(logger).trace(eq("Sending envelope for action {} to destination: {}"), eq(NAME), eq(destination));
    }

    @Test
    public void shouldSendAsAdmin() throws Exception {

        final JmsSender jmsSender = jmsSenderWithComponent(COMMAND_CONTROLLER);

        when(jmsDestinations.getDestination(COMMAND_CONTROLLER, ContextName.fromName(QUEUE_NAME))).thenReturn(destination);
        final JsonEnvelope envelopeWithSysUserId = envelope().with(metadataWithRandomUUID(NAME)).build();
        when(systemUserUtil.asEnvelopeWithSystemUserId(envelope)).thenReturn(envelopeWithSysUserId);

        jmsSender.sendAsAdmin(envelope);
        verify(jmsEnvelopeSender).send(envelopeWithSysUserId, destination);

        verify(logger).trace(eq("Sending envelope for action {} to destination: {}"), eq(NAME), eq(destination));
    }

    private JmsSender jmsSenderWithComponent(final String component) {
        return new JmsSender(component, jmsDestinations, jmsEnvelopeSender, logger, systemUserUtil);
    }

}
