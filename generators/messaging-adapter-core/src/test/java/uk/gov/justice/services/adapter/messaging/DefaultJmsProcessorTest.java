package uk.gov.justice.services.adapter.messaging;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.adapter.messaging.exception.InvalildJmsMessageTypeException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.logging.TraceLogger;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJmsProcessorTest {

    @Mock
    private TextMessage textMessage;

    @Mock
    private JsonEnvelope expectedEnvelope;

    @Mock
    private EnvelopeConverter envelopeConverter;

    @Mock
    private ObjectMessage objectMessage;

    @Mock
    private TraceLogger traceLogger;

    @InjectMocks
    private DefaultJmsProcessor jmsProcessor;

    @Test
    public void shouldPassValidMessageToConsumerFunction() throws Exception {
        when(envelopeConverter.fromMessage(textMessage)).thenReturn(expectedEnvelope);

        jmsProcessor.process(interceptorContext -> assertThat(interceptorContext.inputEnvelope(), is(expectedEnvelope)), textMessage);
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
