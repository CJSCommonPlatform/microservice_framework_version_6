package uk.gov.justice.services.test.utils.core.messaging;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerClientTest {

    private ConsumerClient consumerClient = new ConsumerClient();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private MessageConsumer messageConsumer;

    private String message_1 = "message 1";

    @Test
    public void shouldRetrieveMessagesNoWait() throws Exception {

        TextMessage textMessage = mock(TextMessage.class);

        when(textMessage.getText()).thenReturn(message_1);
        when(messageConsumer.receiveNoWait()).thenReturn(textMessage);

        Optional<String> result = consumerClient.retrieveMessageNoWait(messageConsumer);
        assertThat(result.get(), is(message_1));
        verify(messageConsumer).receiveNoWait();
    }

    @Test
    public void shouldRetrieveMessagesWithAWait() throws Exception {
        TextMessage textMessage = mock(TextMessage.class);

        when(textMessage.getText()).thenReturn(message_1);
        when(messageConsumer.receive(1000)).thenReturn(textMessage);

        Optional<String> result = consumerClient.retrieveMessage(messageConsumer, 1000);
        assertThat(result.get(), is(message_1));
        verify(messageConsumer).receive(1000);
    }

    @Test
    public void shouldDrainTheQueueOnClean() throws Exception {
        final TextMessage textMessage_1 = mock(TextMessage.class);
        final TextMessage textMessage_2 = mock(TextMessage.class);
        final TextMessage textMessage_3 = mock(TextMessage.class);

        when(textMessage_1.getText()).thenReturn("message 1");
        when(textMessage_2.getText()).thenReturn("message 2");
        when(textMessage_3.getText()).thenReturn("message 3");
        when(messageConsumer.receiveNoWait()).thenReturn(textMessage_1, textMessage_2, textMessage_3, null);

        consumerClient.cleanQueue(messageConsumer);
        verify(messageConsumer, times(4)).receiveNoWait();
    }

    @Test
    public void shouldThrowExceptionForNullConsumer() {
        thrown.expect(MessageConsumerException.class);
        thrown.expectMessage("Message consumer not started");
        Optional<String> result = consumerClient.retrieveMessageNoWait(null);
    }

    @Test
    public void shouldThrowExceptionWhenRetrievingMessage() throws Exception {
        thrown.expect(MessageConsumerException.class);
        thrown.expectMessage("Failed to retrieve message");
        when(messageConsumer.receive(10)).thenThrow(JMSException.class);
        Optional<String> result = consumerClient.retrieveMessage(messageConsumer, 10);
    }

}
