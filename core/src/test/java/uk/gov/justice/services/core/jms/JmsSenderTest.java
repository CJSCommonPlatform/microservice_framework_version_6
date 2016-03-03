package uk.gov.justice.services.core.jms;

import com.google.common.io.Resources;
import com.google.common.testing.EqualsTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.jms.converter.EnvelopeConverter;
import uk.gov.justice.services.core.jms.exception.JmsSenderException;
import uk.gov.justice.services.core.util.JsonObjectConverter;
import uk.gov.justice.services.messaging.DefaultEnvelope;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.json.JsonObject;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;

@RunWith(MockitoJUnitRunner.class)
public class JmsSenderTest {

    private static final String QUEUE_NAME = "test.controller.commands";
    private static final String NAME = "test.commands.do-something";

    @Mock
    private EnvelopeConverter envelopeConverter;

    @Mock
    private Envelope envelope;

    @Mock
    private QueueConnectionFactory queueConnectionFactory;

    @Mock
    private Context initialContext;

    @Mock
    private Metadata metadata;

    @Mock
    private JsonObject metadataAsJsonObject;

    @Mock
    private JsonObject payload;

    @Mock
    private QueueSession session;

    @Mock
    private QueueConnection queueConnection;

    @Mock
    private Queue queue;

    @Mock
    private QueueSender sender;

    @Mock
    private TextMessage textMessage;

    @Before
    public void setup() throws Exception {

        when(queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(queueConnectionFactory.createQueueConnection()).thenReturn(queueConnection);
        when(session.createSender(queue)).thenReturn(sender);
        when(metadata.name()).thenReturn(NAME);
        when(session.createTextMessage(anyString())).thenReturn(textMessage);

    }

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    @Test
    public void shouldTestEqualsAndHashCode() {
        final JmsEndpoints jmsEndpoints = new JmsEndpoints();


        final JmsSender item1 = new JmsSender(COMMAND_API, envelopeConverter, jmsEndpoints, queueConnectionFactory);
        final JmsSender item2 = new JmsSender(COMMAND_API, envelopeConverter, jmsEndpoints, queueConnectionFactory);
        final JmsSender item3 = new JmsSender(COMMAND_CONTROLLER, envelopeConverter, jmsEndpoints, queueConnectionFactory);

        new EqualsTester()
                .addEqualityGroup(item1, item2)
                .addEqualityGroup(item3)
                .testEquals();
    }

    @Test
    public void shouldSendValidEnvelopeToTheQueue() throws Exception {

        final JmsSender jmsSender = jmsSenderWithComponent(COMMAND_CONTROLLER);
        when(initialContext.lookup(QUEUE_NAME)).thenReturn(queue);
        final Envelope envelope = testEnvelope();
        when(envelopeConverter.toMessage(envelope, session)).thenReturn(textMessage);

        jmsSender.send(envelope);

        verify(session, times(1)).createSender(queue);
        verify(sender, times(1)).send(textMessage);
        verify(session, times(1)).close();
        verify(queueConnection, times(1)).close();
        verify(sender, times(1)).close();

    }

    @Test(expected = JmsSenderException.class)
    public void shouldThrowExceptionOnQueueCreationError() throws Exception {
        final JmsSender jmsSender = jmsSenderWithComponent(COMMAND_CONTROLLER);
        final Envelope envelope = testEnvelope();
        when(initialContext.lookup(QUEUE_NAME)).thenThrow(new NamingException(""));

        jmsSender.send(envelope);
    }

    @Test(expected = JmsSenderException.class)
    public void shouldThrowExceptionOnContextLookupError() throws Exception {
        final JmsSender jmsSender = jmsSenderWithComponent(COMMAND_CONTROLLER);
        final Envelope envelope = testEnvelope();
        when(initialContext.lookup(QUEUE_NAME)).thenThrow(new NamingException(""));

        jmsSender.send(envelope);
    }

    @Test(expected = JmsSenderException.class)
    public void shouldThrowExceptionWhenContextIsNotConfigured() throws Exception {
        final JmsSender jmsSender = jmsSenderWithComponent(COMMAND_CONTROLLER);
        jmsSender.initialContext = null;

        jmsSender.send(testEnvelope());
    }

    private Envelope testEnvelope() throws IOException {
        final JsonObjectConverter jsonObjectConverter = new JsonObjectConverter();

        final JsonObject expectedEnvelope = jsonObjectConverter.fromString(Resources.toString(Resources.getResource("json/envelope.json"),
                Charset.defaultCharset()));
        final Metadata metadata = JsonObjectMetadata.metadataFrom(expectedEnvelope.getJsonObject(JsonObjectConverter.METADATA));
        final JsonObject payload = jsonObjectConverter.extractPayloadFromEnvelope(expectedEnvelope);

        return DefaultEnvelope.envelopeFrom(metadata, payload);
    }

    private JmsSender jmsSenderWithComponent(final Component component) {
        final JmsSender jmsSender = new JmsSender(component, envelopeConverter, new JmsEndpoints(), queueConnectionFactory);
        jmsSender.initialContext = initialContext;
        return jmsSender;
    }

}