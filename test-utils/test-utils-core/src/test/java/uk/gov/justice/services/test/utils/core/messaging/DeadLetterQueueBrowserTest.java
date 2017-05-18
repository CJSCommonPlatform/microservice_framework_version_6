package uk.gov.justice.services.test.utils.core.messaging;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.json.JsonObject;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeadLetterQueueBrowserTest {

    @Mock
    private Session session;

    @Mock
    private javax.jms.Queue dlqQueue;

    @Mock
    private MessageConsumer dlqMessageConsumer;

    @Mock
    private QueueBrowser dlqBrowser;

    @Mock
    private ConsumerClient consumerClient;

    @Mock
    private JmsSessionFactory jmsSessionFactory;

    @InjectMocks
    private DeadLetterQueueBrowser deadLetterQueueBrowser;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldRemoveMessages() throws JMSException {

        when(session.createConsumer(Mockito.any(javax.jms.Queue.class))).thenReturn(dlqMessageConsumer);

        deadLetterQueueBrowser.removeMessages();

        verify(consumerClient).cleanQueue(dlqMessageConsumer);
    }

    @Test
    public void shouldbeAbleToBrowseAsJson() throws JMSException {
        TextMessage textMessage_1 = mock(TextMessage.class);
        when(textMessage_1.getText()).thenReturn("{\"urn1\": \"urn1\"}", "{\"urn2\": \"urn2\"}");
        Vector<TextMessage> textmessages = new Vector<>(Arrays.asList(textMessage_1, textMessage_1));
        when(session.createBrowser(dlqQueue)).thenReturn(dlqBrowser);
        when(dlqBrowser.getEnumeration()).thenReturn(textmessages.elements());

        List<JsonObject> result = deadLetterQueueBrowser.browseAsJson();

        assertThat(result.size(), is(2));
        assertThat(result, contains(IsMapContaining.hasKey("urn1"), IsMapContaining.hasKey("urn2")));
    }

    @Test
    public void shouldbeAbleToBrowse() throws JMSException {
        TextMessage textMessage_1 = mock(TextMessage.class);
        when(textMessage_1.getText()).thenReturn("abc", "def");
        Vector<TextMessage> textmessages = new Vector<>(Arrays.asList(textMessage_1, textMessage_1));
        when(session.createBrowser(dlqQueue)).thenReturn(dlqBrowser);
        when(dlqBrowser.getEnumeration()).thenReturn(textmessages.elements());

        List<String> result = deadLetterQueueBrowser.browse();

        assertThat(result.size(), is(2));
        assertThat(result, contains("abc", "def"));
    }

    @Test
    public void shouldReturnEmptyListWhenNoElementsInDlq() throws JMSException {
        Vector<TextMessage> textmessages = new Vector<>();
        when(session.createBrowser(dlqQueue)).thenReturn(dlqBrowser);
        when(dlqBrowser.getEnumeration()).thenReturn(textmessages.elements());

        List<String> result = deadLetterQueueBrowser.browse();

        assertThat(result.size(), is(0));
    }

    @Test
    public void shouldThrowExceptionWhenBrowsing() throws JMSException {
        thrown.expect(MessageConsumerException.class);
        thrown.expectMessage("Fatal error getting messges from DLQ");
        when(session.createBrowser(dlqQueue)).thenReturn(dlqBrowser);
        when(dlqBrowser.getEnumeration()).thenThrow(JMSException.class);

        List<String> result = deadLetterQueueBrowser.browse();
    }

    @Test
    public void shouldThrowExceptionWhenCleaningQueue() throws JMSException {
        thrown.expect(MessageConsumerException.class);
        thrown.expectMessage("Fatal error cleaning messges from DLQ");
        when(session.createConsumer(Mockito.any(javax.jms.Queue.class))).thenReturn(dlqMessageConsumer);
        Mockito.doThrow(JMSException.class).when(consumerClient).cleanQueue(dlqMessageConsumer);

        deadLetterQueueBrowser.removeMessages();
    }

    @Test
    public void shouldClose() {
        deadLetterQueueBrowser.close();
        verify(jmsSessionFactory).close();
    }

}
