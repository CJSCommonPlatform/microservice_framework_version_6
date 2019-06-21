package uk.gov.justice.services.messaging.jms;

import static java.lang.String.format;
import static java.util.Collections.list;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

public class JmsQueueBrowser {

    @Resource(mappedName = "java:jboss/DefaultJMSConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Inject
    private DestinationProvider destinationProvider;

    @SuppressWarnings("unchecked")
    public int sizeOf(final String queueName) {

        final Destination destination = destinationProvider.getDestination(queueName);

        if (destination instanceof Queue) {

            try (final Connection connection = connectionFactory.createConnection();
                 final Session session = connection.createSession(false, AUTO_ACKNOWLEDGE);
                 final QueueBrowser queueBrowser = session.createBrowser((Queue) destination)) {

                return list(queueBrowser.getEnumeration()).size();

            } catch (final JMSException e) {
                throw new JmsQueueBrowserException(format("Failed to connect to queue: '%s', when requesting queue size.", queueName), e);
            }
        }

        throw new JmsQueueBrowserUnsupportedOperation(format("The named destination must be a Queue: '%s', unable to get the size of a Topic.", queueName));
    }
}
