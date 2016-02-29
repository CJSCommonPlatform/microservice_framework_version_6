package uk.gov.justice.services.core.jms;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import uk.gov.justice.services.core.jms.exception.JmsSenderException;
import uk.gov.justice.services.core.util.JsonObjectConverter;
import uk.gov.justice.services.messaging.DefaultEnvelope;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import javax.jms.JMSException;
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

@RunWith(MockitoJUnitRunner.class)
public class JmsSenderTest {

    private static final String QUEUE_NAME = "context.command.controller";
    private static final String NAME = "court-names.commands.create-court-name";

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

    @Mock
    private Logger logger;

    private JmsSender jmsSender;

    @Before
    public void setup() throws JMSException, NamingException {
        jmsSender = new JmsSender();
        jmsSender.logger = logger;
        jmsSender.queueConnectionFactory = queueConnectionFactory;
        jmsSender.jsonObjectConverter = new JsonObjectConverter();
        when(queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(queueConnectionFactory.createQueueConnection()).thenReturn(queueConnection);
        when(session.createSender(queue)).thenReturn(sender);

        when(metadata.name()).thenReturn(NAME);
        when(session.createTextMessage(anyString())).thenReturn(textMessage);
    }

    @Test
    public void shouldSendValidEnvelopeToTheQueue() throws Exception {
        jmsSender.initialContext = initialContext;

        when(initialContext.lookup(QUEUE_NAME)).thenReturn(queue);

        final Envelope envelope = getEnvelope();

        jmsSender.send(QUEUE_NAME, envelope);

        verify(textMessage, times(1)).setStringProperty(JmsSender.JMS_HEADER_CPPNAME, NAME);
        verify(session, times(1)).createSender(queue);
        verify(sender, times(1)).send(textMessage);
        verify(session, times(1)).close();
        verify(queueConnection, times(1)).close();
        verify(sender, times(1)).close();


    }

    @Test(expected = JmsSenderException.class)
    public void shouldThrowExceptionOnQueueCreationError() throws Exception {
        jmsSender.initialContext = initialContext;
        final Envelope envelope = getEnvelope();

        when(initialContext.lookup(QUEUE_NAME)).thenThrow(new NamingException(""));
        jmsSender.send(QUEUE_NAME, envelope);
    }

    @Test(expected = JmsSenderException.class)
    public void shouldThrowExceptionOnContextLookupError() throws Exception {
        jmsSender.initialContext = initialContext;
        final Envelope envelope = getEnvelope();

        when(initialContext.lookup(QUEUE_NAME)).thenThrow(new NamingException(""));
        jmsSender.send(QUEUE_NAME, envelope);
    }

    @Test(expected = JmsSenderException.class)
    public void shouldThrowExceptionWhenContextIsNotConfigured() throws Exception {
        jmsSender.send(QUEUE_NAME, getEnvelope());
    }

    private Envelope getEnvelope() throws IOException {
        JsonObjectConverter jsonObjectConverter = new JsonObjectConverter();

        JsonObject expectedEnvelope = jsonObjectConverter.fromString(Resources.toString(Resources.getResource("json/envelope.json"),
                Charset.defaultCharset()));
        Metadata metadata = JsonObjectMetadata.metadataFrom(expectedEnvelope.getJsonObject(JsonObjectConverter.METADATA));
        JsonObject payload = jsonObjectConverter.extractPayloadFromEnvelope(expectedEnvelope);

        return DefaultEnvelope.envelopeFrom(metadata, payload);
    }

}