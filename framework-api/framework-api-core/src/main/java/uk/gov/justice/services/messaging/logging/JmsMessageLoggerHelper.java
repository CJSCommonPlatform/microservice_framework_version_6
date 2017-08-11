package uk.gov.justice.services.messaging.logging;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.json.JsonObject;

public interface JmsMessageLoggerHelper {

    String toJmsTraceString(final Message message);

    JsonObject metadataAsJsonObject(final TextMessage message) throws JMSException;
}