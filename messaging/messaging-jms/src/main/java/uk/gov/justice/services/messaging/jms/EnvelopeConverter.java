package uk.gov.justice.services.messaging.jms;

import static uk.gov.justice.services.messaging.jms.HeaderConstants.JMS_HEADER_CPPNAME;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.exception.JmsConverterException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Implementation of {@link MessageConverter} for {@link JsonEnvelope}
 */
@ApplicationScoped
public class EnvelopeConverter implements MessageConverter<JsonEnvelope, TextMessage> {

    @Inject
    StringToJsonObjectConverter stringToJsonObjectConverter;

    @Inject
    JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Override
    public JsonEnvelope fromMessage(final TextMessage message) {
        String messageAsString;

        try {
            messageAsString = message.getText();
            return jsonObjectEnvelopeConverter.asEnvelope(stringToJsonObjectConverter.convert(messageAsString));
        } catch (JMSException e) {
            throw createJmsConverterException(message, e);
        }
    }

    @Override
    public TextMessage toMessage(final JsonEnvelope envelope, final Session session) {
        final String envelopeAsString = jsonObjectEnvelopeConverter.fromEnvelope(envelope).toString();

        try {
            final TextMessage textMessage = session.createTextMessage(envelopeAsString);
            textMessage.setStringProperty(JMS_HEADER_CPPNAME, envelope.metadata().name());
            return textMessage;
        } catch (JMSException e) {
            throw new JmsConverterException(String.format("Exception while creating message from envelope %s", envelopeAsString), e);
        }
    }

    private JmsConverterException createJmsConverterException(final TextMessage message, final Throwable e) {
        try {
            return new JmsConverterException(String.format("Exception while creating envelope from message %s", message.getJMSMessageID()), e);
        } catch (JMSException e1) {
            return new JmsConverterException(String.format("Exception while creating envelope from message. Failed to retrieve messageId from %s", message), e1);
        }
    }

}
