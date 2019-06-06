package uk.gov.justice.services.messaging.jms;

import static java.util.Arrays.asList;
import static java.util.Collections.enumeration;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.Topic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JmsQueueBrowserTest {

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private DestinationProvider destinationProvider;

    @InjectMocks
    private JmsQueueBrowser jmsQueueBrowser;

    @Test
    public void shouldReturnSizeOfQueue() throws Exception {

        final String destinationName = "queue-name";
        final Queue queue = mock(Queue.class);
        final Connection connection = mock(Connection.class);
        final Session session = mock(Session.class);
        final QueueBrowser queueBrowser = mock(QueueBrowser.class);
        final Message message = mock(Message.class);

        when(destinationProvider.getDestination(destinationName)).thenReturn(queue);
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createBrowser(queue)).thenReturn(queueBrowser);
        when(queueBrowser.getEnumeration()).thenReturn(enumeration(asList(message, message, message, message, message)));

        final int queueSize = jmsQueueBrowser.sizeOf(destinationName);

        assertThat(queueSize, is(5));
    }

    @Test
    public void shouldThrowJmsQueueBrowserExceptionIfJMSExceptionThrown() throws Exception {

        final String destinationName = "queue-name";
        final Queue queue = mock(Queue.class);
        final JMSException jmsException = new JMSException("Test Message");

        when(destinationProvider.getDestination(destinationName)).thenReturn(queue);
        when(connectionFactory.createConnection()).thenThrow(jmsException);

        try {
            jmsQueueBrowser.sizeOf(destinationName);
            fail();
        } catch (final JmsQueueBrowserException exception) {
            assertThat(exception.getMessage(), is("Failed to connect to queue: 'queue-name', when requesting queue size."));
        }
    }

    @Test
    public void shouldThrowJmsQueueBrowserUnsupportedOperationIfQueueNameIsNotAQueue() throws Exception {

        final String destinationName = "topic-name";
        final Topic topic = mock(Topic.class);

        when(destinationProvider.getDestination(destinationName)).thenReturn(topic);

        try {
            jmsQueueBrowser.sizeOf(destinationName);
            fail();
        } catch (final JmsQueueBrowserUnsupportedOperation exception) {
            assertThat(exception.getMessage(), is("The named destination must be a Queue: 'topic-name', unable to get the size of a Topic."));
        }
    }
}