package uk.gov.justice.services.messaging.spi;

import static uk.gov.justice.services.messaging.JSONObjectValueObfuscator.obfuscated;
import static uk.gov.justice.services.messaging.JsonMetadata.CORRELATION;
import static uk.gov.justice.services.messaging.JsonMetadata.SESSION_ID;
import static uk.gov.justice.services.messaging.JsonMetadata.USER_ID;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Default implementation of an envelope.
 */
public class DefaultJsonEnvelope implements JsonEnvelope {

    private Metadata metadata;

    private JsonValue payload;

    DefaultJsonEnvelope(final Metadata metadata, final JsonValue payload) {
        this.metadata = metadata;
        this.payload = payload;
    }

    @Override
    public Metadata metadata() {
        return metadata;
    }

    @Override
    public JsonValue payload() {
        return payload;
    }

    @Override
    public JsonObject payloadAsJsonObject() {
        return (JsonObject) payload;
    }

    @Override
    public JsonArray payloadAsJsonArray() {
        return (JsonArray) payload;
    }

    @Override
    public JsonNumber payloadAsJsonNumber() {
        return (JsonNumber) payload;
    }

    @Override
    public JsonString payloadAsJsonString() {
        return (JsonString) payload;
    }

    @Override
    public JsonObject asJsonObject() {
        return createObjectBuilder(payloadAsJsonObject())
                .add(METADATA, metadata().asJsonObject()).build();
    }

    @Override
    public String toString() {
        final JsonObjectBuilder builder = Json.createObjectBuilder();

        if (metadata != null) {
            builder.add("id", String.valueOf(metadata.id()))
                    .add("name", metadata.name());


            metadata.clientCorrelationId().ifPresent(s -> builder.add(CORRELATION, s));
            metadata.sessionId().ifPresent(s -> builder.add(SESSION_ID, s));
            metadata.userId().ifPresent(s -> builder.add(USER_ID, s));

            final JsonArrayBuilder causationBuilder = Json.createArrayBuilder();

            final List<UUID> causes = metadata.causation();

            if (causes != null) {
                metadata.causation().forEach(uuid -> causationBuilder.add(String.valueOf(uuid)));
            }
            builder.add("causation", causationBuilder);
        }
        return builder.build().toString();
    }

    @Override
    public String toDebugStringPrettyPrint() {

        return jSONPayload().put(METADATA, new JSONObject(metadata.asJsonObject().toString())).toString(2);
    }

    @Override
    public String toObfuscatedDebugString() {
        return obfuscated(jSONPayload()).put(METADATA, new JSONObject(metadata.asJsonObject().toString())).toString(2);
    }


    private JSONObject jSONPayload() {
        return new JSONObject(new JSONTokener(payload.toString()));
    }


}
