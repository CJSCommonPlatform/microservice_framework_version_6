package uk.gov.justice.services.core.jms;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.jms.exception.InvalildJmsMessageTypeException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractJMSListenerTest {

    @Mock
    private Dispatcher dispatcher;

    @Mock
    private TextMessage textMessage;

    @Mock
    private ObjectMessage objectMessage;

    @Mock
    private Envelope envelope;

    @Mock
    private EnvelopeConverter envelopeConverter;

    private AbstractJMSListener jmsListener;

    @Before
    public void setup() throws JMSException, IOException {
        jmsListener = new TestJMSListener();
        jmsListener.envelopeConverter = envelopeConverter;
    }

    @Test
    public void shouldReturnDispatcher() throws Exception {
        assertThat(new TestJMSListener().getDispatcher(), equalTo(dispatcher));
    }

    @Test
    public void shouldDispatchEnvelope() throws Exception {
        final ArgumentCaptor<Envelope> captor = ArgumentCaptor.forClass(Envelope.class);
        when(envelopeConverter.fromMessage(textMessage)).thenReturn(envelope);

        jmsListener.onMessage(textMessage);

        verify(dispatcher).dispatch(captor.capture());

        final Envelope actualEnvelope = captor.getValue();
        assertThat(actualEnvelope.metadata(), equalTo(envelope.metadata()));
        assertThat(actualEnvelope.payload(), equalTo(envelope.payload()));
    }

    @Test(expected = InvalildJmsMessageTypeException.class)
    public void shouldThrowExceptionWithWrongMessageType() throws Exception {
        jmsListener.onMessage(objectMessage);
    }

    @Test(expected = InvalildJmsMessageTypeException.class)
    public void shouldThrowExceptionWhenFailToRetrieveMessageId() throws Exception {
        doThrow(JMSException.class).when(objectMessage).getJMSMessageID();

        jmsListener.onMessage(objectMessage);
    }

    public class TestJMSListener extends AbstractJMSListener {

        @Override
        protected Dispatcher getDispatcher() {
            return dispatcher;
        }
    }

}