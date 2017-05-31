package uk.gov.justice.services.test.utils.core.messaging;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.MessageConsumerClient.QUEUE_URI;
import static uk.gov.justice.services.test.utils.core.messaging.MessageConsumerClient.TIMEOUT_IN_MILLIS;

import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MessageConsumerClientTest {

    @Mock
    private MessageConsumerFactory messageConsumerFactory;

    @Mock
    private ConsumerClient consumerClient;

    @InjectMocks
    private MessageConsumerClient messageConsumerClient;


    @Test
    public void shouldCreateAndStartAMessageConsumer() throws Exception {

        final String eventName = "my-context.events.my-event";
        final String topicName = "my-context.event";
        final String messageSelector = "CPPNAME IN ('my-context.events.my-event')";

        final MessageConsumer messageConsumer = mock(MessageConsumer.class);

        when(messageConsumerFactory.createAndStart(messageSelector, QUEUE_URI, topicName)).thenReturn(messageConsumer);

        messageConsumerClient.startConsumer(eventName, topicName);

        assertThat(messageConsumerClient.getMessageConsumer(), is(messageConsumer));
    }

    @Test
    public void shouldCloseTheConsumerFactoryIfStartingTheMessageConsumerFails() throws Exception {

        final JMSException jmsException = new JMSException("Ooops");

        final String eventName = "my-context.events.my-event";
        final String topicName = "my-context.event";
        final String messageSelector = "CPPNAME IN ('my-context.events.my-event')";
        final String queueUri = QUEUE_URI;

        when(messageConsumerFactory.createAndStart(messageSelector, queueUri, topicName)).thenThrow(jmsException);

        try {
            messageConsumerClient.startConsumer(eventName, topicName);
            fail();
        } catch (final MessageConsumerException expected) {
            assertThat(expected.getCause(), is(jmsException));
            assertThat(expected.getMessage(), is("Failed to start message consumer for " +
                    "eventName 'my-context.events.my-event':, " +
                    "topic: 'my-context.event', " +
                    "queueUri: 'tcp://localhost:61616', " +
                    "messageSelector: 'CPPNAME IN ('my-context.events.my-event')'"));
        }

        verify(messageConsumerFactory).close();
    }

    @Test
    public void shouldRetrieveMessagesWithATimeout() throws Exception {

        final long timeout = 239847L;
        final String eventName = "my-context.events.my-event";
        final String topicName = "my-context.event";
        final String messageSelector = "CPPNAME IN ('my-context.events.my-event')";
        final TextMessage textMessage = mock(TextMessage.class);

        final MessageConsumer messageConsumer = mock(MessageConsumer.class);
        final String message = "message";

        when(messageConsumerFactory.createAndStart(messageSelector, QUEUE_URI, topicName)).thenReturn(messageConsumer);


        messageConsumerClient.startConsumer(eventName, topicName);

        when(consumerClient.retrieveMessage(messageConsumer,timeout)).thenReturn(Optional.of(message));

        final String actualMessage = messageConsumerClient
                .retrieveMessage(timeout)
                .orElseThrow(() -> new AssertionError("Failed to get message"));

        assertThat(actualMessage, is(message));
    }

    @Test
    public void shouldThrowARuntimeExceptionIfTheMessageConsumerIsNotStartedBeforeRetrievingMessages() throws Exception {

        final long timeout = 239847L;

        try {
            when(consumerClient.retrieveMessage(null, timeout)).thenThrow(new MessageConsumerException("Message consumer not started"));
            messageConsumerClient.retrieveMessage(timeout);
            fail();
        } catch (final MessageConsumerException expected) {
            assertThat(expected.getMessage(), is("Message consumer not started"));
        }
    }

    @Test
    public void shouldThrowARuntimeExceptionIfRetrievingMessagesWithATimeoutFails() throws Exception {

        final JMSException jmsException = new JMSException("Ooops");

        final long timeout = 239847L;
        final String eventName = "my-context.events.my-event";
        final String topicName = "my-context.event";
        final String messageSelector = "CPPNAME IN ('my-context.events.my-event')";


        final MessageConsumer messageConsumer = mock(MessageConsumer.class);

        when(messageConsumerFactory.createAndStart(messageSelector, QUEUE_URI, topicName)).thenReturn(messageConsumer);


        messageConsumerClient.startConsumer(eventName, topicName);


        when(consumerClient.retrieveMessage(messageConsumer, timeout)).thenThrow(new MessageConsumerException("Failed to retrieve message", jmsException));

        try {
            messageConsumerClient.retrieveMessage(timeout);
            fail();
        } catch (final MessageConsumerException expected) {
            assertThat(expected.getMessage(), is("Failed to retrieve message"));
            assertThat(expected.getCause(), is(jmsException));
        }
    }

    @Test
    public void shouldRetrieveMessagesWithDefaultTimeout() throws Exception {

        final String eventName = "my-context.events.my-event";
        final String topicName = "my-context.event";
        final String messageSelector = "CPPNAME IN ('my-context.events.my-event')";

        final MessageConsumer messageConsumer = mock(MessageConsumer.class);
        final String message = "message";

        when(messageConsumerFactory.createAndStart(messageSelector, QUEUE_URI, topicName)).thenReturn(messageConsumer);

        messageConsumerClient.startConsumer(eventName, topicName);

        when(consumerClient.retrieveMessage(messageConsumer, TIMEOUT_IN_MILLIS)).thenReturn(Optional.of(message));

        final String actualMessage = messageConsumerClient
                .retrieveMessage()
                .orElseThrow(() -> new AssertionError("Failed to get message"));

        assertThat(actualMessage, is(message));
    }

    @Test
    public void shouldRetrieveMessagesWithNoWait() throws Exception {

        final String eventName = "my-context.events.my-event";
        final String topicName = "my-context.event";
        final String messageSelector = "CPPNAME IN ('my-context.events.my-event')";

        final MessageConsumer messageConsumer = mock(MessageConsumer.class);
        final String message = "message";

        when(messageConsumerFactory.createAndStart(messageSelector, QUEUE_URI, topicName)).thenReturn(messageConsumer);

        messageConsumerClient.startConsumer(eventName, topicName);

        when(consumerClient.retrieveMessageNoWait(messageConsumer)).thenReturn(Optional.of(message));

        final String actualMessage = messageConsumerClient
                .retrieveMessageNoWait()
                .orElseThrow(() -> new AssertionError("Failed to get message"));

        assertThat(actualMessage, is(message));
    }

    @Test
    public void shouldDrainTheQueueOnClean() throws Exception {

        final String eventName = "my-context.events.my-event";
        final String topicName = "my-context.event";
        final String messageSelector = "CPPNAME IN ('my-context.events.my-event')";


        final MessageConsumer messageConsumer = mock(MessageConsumer.class);

        when(messageConsumerFactory.createAndStart(messageSelector, QUEUE_URI, topicName)).thenReturn(messageConsumer);

        messageConsumerClient.startConsumer(eventName, topicName);

        messageConsumerClient.cleanQueue();

        verify(consumerClient).cleanQueue(messageConsumer);
    }

    @Test
    public void shouldCloseTheMessageConsumerFactoryOnClose() throws Exception {

        final String eventName = "my-context.events.my-event";
        final String topicName = "my-context.event";
        final String messageSelector = "CPPNAME IN ('my-context.events.my-event')";

        final MessageConsumer messageConsumer = mock(MessageConsumer.class);

        when(messageConsumerFactory.createAndStart(messageSelector, QUEUE_URI, topicName)).thenReturn(messageConsumer);

        messageConsumerClient.startConsumer(eventName, topicName);

        messageConsumerClient.close();

        verify(messageConsumerFactory).close();
    }
}
