package uk.gov.justice.services.test.utils.core.messaging;

import static uk.gov.justice.services.test.utils.core.messaging.QueueUriProvider.artemisQueueUri;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;


/**
 * Utility class that allows to browse and clean
 * messages in dead letter queue
 *
 * Usage: DeadLetterQueueBrowser dlqBrowser = new DeadLetterQueueBrowser();
 * dlqBrowser.browse() will return a list of {@link String} in the dlq
 * dlqBrowser.removeMessages() will clean dlq
 * dlqBrowser.close() will release resources
 *
 * Note:It has been observed there is sometimes a delay by the time
 * the message lands in dlq. Setting a delay of few milliseconds
 * generally resolves this.
 *
 * @author gopal
 *
 */
public class DeadLetterQueueBrowser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeadLetterQueueBrowser.class);

    private static final String DLQ_QUEUE_URI = artemisQueueUri();
    private static final String dlqName = "DLQ";

    private Session session;
    private javax.jms.Queue dlqQueue;
    private final JmsSessionFactory jmsSessionFactory;
    private final ConsumerClient consumerClient;

    public DeadLetterQueueBrowser() {
        jmsSessionFactory = new JmsSessionFactory();
        consumerClient = new ConsumerClient();
        initialise();
    }

    @VisibleForTesting
    public DeadLetterQueueBrowser(final Queue dlqQueue, final Session session,
            final JmsSessionFactory jmsSessionFactory, final ConsumerClient consumerClient) {
        super();
        this.session = session;
        this.dlqQueue = dlqQueue;
        this.jmsSessionFactory = jmsSessionFactory;
        this.consumerClient = consumerClient;
    }

    private void initialise() {
        try {
            LOGGER.info("Artemis URI: {}", DLQ_QUEUE_URI);
            session = jmsSessionFactory.session(DLQ_QUEUE_URI);
            dlqQueue = new ActiveMQQueue(dlqName);
        } catch (Exception e) {
            close();
            final String message = "Failed to start dlq message consumer for " + "queue: '" + dlqName + "', "
                    + "queueUri: '" + DLQ_QUEUE_URI + " ";
            LOGGER.error("Fatal error initialising Artemis {}  ", message);
            throw new MessageConsumerException(message, e);
        }
    }

    /**
     * allows browsing messages in dlq
     * @return list of {@link JsonObject}
     */
    public List<JsonObject> browseAsJson() {
        return browse().stream().map(s->convert(s)).collect(Collectors.toCollection(ArrayList<JsonObject>::new));
    }

    /**
     * allows browsing messages in dlq
     * @return list of {@link String}
     */
    public List<String> browse() {
        try (QueueBrowser dlqBrowser = session.createBrowser(dlqQueue);) {
            final List<String> messages = new ArrayList<>();
            final Enumeration enumeration = dlqBrowser.getEnumeration();

            while (enumeration.hasMoreElements()) {
                String message = ((TextMessage) enumeration.nextElement()).getText();
                messages.add(message);
            }
            return messages;
        } catch (JMSException e) {
            String message = "Fatal error getting messges from DLQ";
            LOGGER.error(message);
            throw new MessageConsumerException(message, e);
        }
    }

    /**
     * removes messages from dlq
     */
    public void removeMessages() {
        try (MessageConsumer messageConsumer = session.createConsumer(dlqQueue)) {
            cleanQueue(messageConsumer);
        } catch (JMSException e) {
            String message = "Fatal error cleaning messges from DLQ";
            LOGGER.error(message);
            throw new MessageConsumerException(message, e);
        }
    }

    private void cleanQueue(MessageConsumer messageConsumer) {
        consumerClient.cleanQueue(messageConsumer);
    }

    private JsonObject convert(final String source) {
        try (final JsonReader reader = Json.createReader(new StringReader(source))) {
            return reader.readObject();
        }
    }

    /**
     * clean up resources
     */
    public void close() {
        jmsSessionFactory.close();
    }

}
