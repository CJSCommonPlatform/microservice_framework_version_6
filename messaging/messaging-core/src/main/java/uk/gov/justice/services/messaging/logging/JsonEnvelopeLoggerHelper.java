package uk.gov.justice.services.messaging.logging;

import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.List;
import java.util.UUID;

import static uk.gov.justice.services.messaging.JsonObjectMetadata.CORRELATION;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.SESSION_ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.USER_ID;

/**
 * Helper class to provide trace string for logging of JsonEnvelopes
 */
public final class JsonEnvelopeLoggerHelper {

    private JsonEnvelopeLoggerHelper() {}

    public static String toEnvelopeTraceString(final JsonEnvelope envelope) {

        final JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("id", String.valueOf(envelope.metadata().id()))
                .add("name", envelope.metadata().name());

        envelope.metadata().clientCorrelationId().ifPresent(s -> builder.add(CORRELATION, s));
        envelope.metadata().sessionId().ifPresent(s -> builder.add(SESSION_ID, s));
        envelope.metadata().userId().ifPresent(s -> builder.add(USER_ID, s));

        final JsonArrayBuilder causationBuilder = Json.createArrayBuilder();

        final List<UUID> causes = envelope.metadata().causation();

        if(causes != null) {
            envelope.metadata().causation().stream()
                    .forEach(uuid -> causationBuilder.add(String.valueOf(uuid)));
        }
        return builder.add("causation", causationBuilder).build().toString();
    }
}
