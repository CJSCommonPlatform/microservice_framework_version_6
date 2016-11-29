package uk.gov.justice.services.messaging.logging;

import static uk.gov.justice.services.common.log.LoggerConstants.METADATA;
import static uk.gov.justice.services.common.log.LoggerConstants.SERVICE_CONTEXT;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public final class JmsMessageLoggerHelper {

    private JmsMessageLoggerHelper() {
    }

    public static String toJmsTraceString(final Message message) {
        try {
            return metadataAsJsonObject((TextMessage) message).toString();
        } catch (Exception e) {
            return "Could not find: _metadata in message";

        }
    }

    public static void addMetadataToJsonBuilder(final Message message, final JsonObjectBuilder builder) {
        try {
            builder.add(METADATA, metadataAsJsonObject((TextMessage) message));
        } catch (Exception e) {
            builder.add(METADATA, "Could not find: _metadata in message");
        }
    }

    public static void addServiceContextNameIfPresent(final ServiceContextNameProvider serviceContextNameProvider, final JsonObjectBuilder builder) {
        Optional.ofNullable(serviceContextNameProvider.getServiceContextName())
                .ifPresent(value -> builder.add(SERVICE_CONTEXT, value));
    }

    private static JsonObject metadataAsJsonObject(final TextMessage message) throws JMSException {
        return new JsonObjectEnvelopeConverter()
                .asEnvelope(new StringToJsonObjectConverter().convert(message.getText()))
                .metadata()
                .asJsonObject();
    }
}
