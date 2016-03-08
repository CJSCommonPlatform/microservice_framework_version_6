package uk.gov.justice.services.core.jms;


import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.jms.exception.InvalildJmsMessageTypeException;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Abstract JMS Listener. Dispatches the message to the correct handler.
 */
public abstract class AbstractJMSListener implements MessageListener {

    @Inject
    EnvelopeConverter envelopeConverter;

    protected abstract Dispatcher getDispatcher();

    @Override
    public void onMessage(final Message message) {
        if (!(message instanceof TextMessage)) {
            try {
                throw new InvalildJmsMessageTypeException(String.format("Message is not an instance of TextMessage %s", message.getJMSMessageID()));
            } catch (JMSException e) {
                throw new InvalildJmsMessageTypeException(String.format("Message is not an instance of TextMessage. Failed to retrieve messageId %s",
                        message), e);
            }
        }

        getDispatcher().dispatch(envelopeConverter.fromMessage((TextMessage) message));
    }

}
