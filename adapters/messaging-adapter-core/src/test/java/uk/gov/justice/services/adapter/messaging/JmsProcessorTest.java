package uk.gov.justice.services.adapter.messaging;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.adapter.messaging.exception.InvalildJmsMessageTypeException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JmsProcessorTest {

    @Mock
    private TextMessage textMessage;

    @Mock
    private JsonEnvelope expectedJsonEnvelope;

    @Mock
    private EnvelopeConverter envelopeConverter;

    @Mock
    private ObjectMessage objectMessage;

    private JmsProcessor jmsProcessor;

    @Before
    public void setup() throws JMSException, IOException {
        jmsProcessor = new JmsProcessor();
        jmsProcessor.envelopeConverter = envelopeConverter;
    }

    @Test
    public void shouldPassValidMessageToConsumerFunction() throws Exception {
        when(envelopeConverter.fromMessage(textMessage)).thenReturn(expectedJsonEnvelope);

        jmsProcessor.process(envelope -> assertThat(envelope, is(expectedJsonEnvelope)), textMessage);
    }

    @Test(expected = InvalildJmsMessageTypeException.class)
    public void shouldThrowExceptionWithWrongMessageType() throws Exception {
        jmsProcessor.process(envelope -> {
        }, objectMessage);
    }

    @Test(expected = InvalildJmsMessageTypeException.class)
    public void shouldThrowExceptionWhenFailToRetrieveMessageId() throws Exception {
        doThrow(JMSException.class).when(objectMessage).getJMSMessageID();

        jmsProcessor.process(envelope -> {
        }, objectMessage);
    }

}