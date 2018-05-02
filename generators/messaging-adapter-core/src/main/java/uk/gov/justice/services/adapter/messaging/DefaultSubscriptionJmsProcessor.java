package uk.gov.justice.services.adapter.messaging;

import static java.lang.String.format;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper;
import uk.gov.justice.services.messaging.logging.TraceLogger;
import uk.gov.justice.services.subscription.SubscriptionManager;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In order to minimise the amount of generated code in the JMS Listener implementation classes,
 * this service encapsulates all the logic for validating a message and converting it to an
 * envelope, passing it to a subscription manager to manage the message.  This allows testing of
 * this logic independently from the automated generation code.
 */
public class DefaultSubscriptionJmsProcessor implements SubscriptionJmsProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSubscriptionJmsProcessor.class);

    @Inject
    EnvelopeConverter envelopeConverter;

    @Inject
    TraceLogger traceLogger;

    @Inject
    JmsMessageLoggerHelper jmsMessageLoggerHelper;

    @Override
    public void process(final SubscriptionManager subscriptionManager, final Message message) {

        traceLogger.trace(LOGGER, () -> format("Processing JMS message: %s", jmsMessageLoggerHelper.toJmsTraceString(message)));

        if (!(message instanceof TextMessage)) {
            try {
                throw new InvalildJmsMessageTypeException(format("Message is not an instance of TextMessage %s", message.getJMSMessageID()));
            } catch (JMSException e) {
                throw new InvalildJmsMessageTypeException(format("Message is not an instance of TextMessage. Failed to retrieve messageId %s",
                        message), e);
            }
        }

        final JsonEnvelope jsonEnvelope = envelopeConverter.fromMessage((TextMessage) message);
        traceLogger.trace(LOGGER, () -> format("JMS message converted to envelope: %s", jsonEnvelope));
        subscriptionManager.process(jsonEnvelope);
        traceLogger.trace(LOGGER, () -> format("JMS message processed: %s", jsonEnvelope));
    }
}
