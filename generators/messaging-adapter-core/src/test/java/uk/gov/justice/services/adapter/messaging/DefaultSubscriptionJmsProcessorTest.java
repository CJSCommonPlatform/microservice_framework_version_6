package uk.gov.justice.services.adapter.messaging;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.logging.TraceLogger;
import uk.gov.justice.services.subscription.SubscriptionManager;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSubscriptionJmsProcessorTest {

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
    private DefaultSubscriptionJmsProcessor subscriptionJmsProcessor;

    @Test
    public void shouldPassValidMessageToConsumerFunction() throws Exception {
        final SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);
        when(envelopeConverter.fromMessage(textMessage)).thenReturn(expectedEnvelope);

        subscriptionJmsProcessor.process(textMessage, subscriptionManager);

        verify(subscriptionManager).process(expectedEnvelope);
    }

    @Test(expected = InvalildJmsMessageTypeException.class)
    public void shouldThrowExceptionWithWrongMessageType() throws Exception {
        final SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);
        subscriptionJmsProcessor.process(objectMessage, subscriptionManager);
    }

    @Test(expected = InvalildJmsMessageTypeException.class)
    public void shouldThrowExceptionWhenFailToRetrieveMessageId() throws Exception {
        final SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);

        doThrow(JMSException.class).when(objectMessage).getJMSMessageID();

        subscriptionJmsProcessor.process(objectMessage, subscriptionManager);
    }
}