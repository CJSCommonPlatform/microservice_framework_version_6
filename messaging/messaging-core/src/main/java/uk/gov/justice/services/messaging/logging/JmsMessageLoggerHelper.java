package uk.gov.justice.services.messaging.logging;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

public final class JmsMessageLoggerHelper {

    private JmsMessageLoggerHelper(){}

    public static String toJmsTraceString(final Message message) {
        try {
            return new JsonObjectEnvelopeConverter().asEnvelope(new StringToJsonObjectConverter()
                    .convert(((TextMessage)message).getText()))
                    .metadata().asJsonObject().toString();
        } catch (Exception e) {
            return "Could not find: _metadata in message";
        }
    }
}
