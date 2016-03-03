package uk.gov.justice.services.core.util;

import com.google.common.io.Resources;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.justice.services.messaging.DefaultEnvelope;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JsonObjectConverterTest {

    private static final String ID = "861c9430-7bc6-4bf0-b549-6534394b8d65";
    private static final String NAME = "test.commands.do-something";
    private static final String CLIENT = "d51597dc-2526-4c71-bd08-5031c79f11e1";
    private static final String SESSION = "45b0c3fe-afe6-4652-882f-7882d79eadd9";
    private static final String USER = "72251abb-5872-46e3-9045-950ac5bae399";
    private static final String CAUSATION_1 = "cd68037b-2fcf-4534-b83d-a9f08072f2ca";
    private static final String CAUSATION_2 = "43464b22-04c1-4d99-8359-82dc1934d763";

    private static final String PAYLOAD_ID = "c3f7182b-bd20-4678-ba8b-e7e5ea8629c3";
    private static final Long PAYLOAD_VERSION = 0L;
    private static final String PAYLOAD_NAME = "Name of the Payload";

    @Test
    public void shouldReturnJsonObjectFromString() throws Exception {
        JsonObjectConverter jsonObjectConverter = new JsonObjectConverter();

        JsonObject joEnvelope = jsonObjectConverter.fromString(jsonFromFile("envelope"));

        assertThat(joEnvelope, notNullValue());
        JsonObject joMetadata = joEnvelope.getJsonObject(JsonObjectConverter.METADATA);
        JsonObject joPayload = jsonObjectConverter.extractPayloadFromEnvelope(joEnvelope);
        assertThat(joMetadata, notNullValue());
        assertThat(joPayload, notNullValue());

        assertThat(joMetadata.getString("id"), equalTo(ID));
        assertThat(joMetadata.getString("name"), equalTo(NAME));
        JsonObject correlation = joMetadata.getJsonObject("correlation");
        assertThat(correlation, notNullValue());
        assertThat(correlation.getString("client"), equalTo(CLIENT));

        JsonObject context = joMetadata.getJsonObject("context");
        assertThat(context, notNullValue());
        assertThat(context.getString("session"), equalTo(SESSION));
        assertThat(context.getString("user"), equalTo(USER));

        JsonArray causation = joMetadata.getJsonArray(Metadata.CAUSATION);
        assertThat(causation, notNullValue());
        assertThat(causation.size(), equalTo(2));
        assertThat(causation.get(0).toString().replaceAll("\"", ""), equalTo(CAUSATION_1));
        assertThat(causation.get(1).toString().replaceAll("\"", ""), equalTo(CAUSATION_2));

        assertThat(joEnvelope.getString("payloadId"), equalTo(PAYLOAD_ID));
        assertThat((long) joEnvelope.getInt("payloadVersion"), equalTo(PAYLOAD_VERSION));
        assertThat(joEnvelope.getString("payloadName"), equalTo(PAYLOAD_NAME));

    }

    @Test
    public void shouldReturnStringFromJsonObject() throws IOException {
        JsonObjectConverter jsonObjectConverter = new JsonObjectConverter();
        
        String envelopeAsString = jsonObjectConverter.asString(envelopeAsJsonObject());

        assertThat(envelopeAsString, notNullValue());
        JSONAssert.assertEquals("{payloadId:" + PAYLOAD_ID + "}", envelopeAsString, false);
        JSONAssert.assertEquals("{payloadName:" + PAYLOAD_NAME + "}", envelopeAsString, false);
        JSONAssert.assertEquals("{payloadVersion:" + PAYLOAD_VERSION + "}", envelopeAsString, false);
        JSONAssert.assertEquals("{_metadata:{id:" + ID + "}}", envelopeAsString, false);
        JSONAssert.assertEquals("{_metadata:{name:" + NAME + "}}", envelopeAsString, false);

    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnMissingId() throws Exception {
        final JsonObject joEnvelope = new JsonObjectConverter().fromString(jsonFromFile("json/envelope-missing-id.json"));
        JsonObjectMetadata.metadataFrom(joEnvelope.getJsonObject(JsonObjectConverter.METADATA));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnMissingName() throws Exception {
        final JsonObject joEnvelope = new JsonObjectConverter().fromString(jsonFromFile("envelope-missing-name"));
        JsonObjectMetadata.metadataFrom(joEnvelope.getJsonObject(JsonObjectConverter.METADATA));
    }

    @Test
    public void shouldReturnEnvelope() throws Exception {
        final JsonObjectConverter jsonObjectConverter = new JsonObjectConverter();

        Envelope envelope = jsonObjectConverter.asEnvelope(jsonObjectConverter.fromString(jsonFromFile("envelope")));

        assertThat(envelope, notNullValue());
        Metadata metadata = envelope.metadata();
        JsonObject payload = envelope.payload();
        assertThat(metadata, notNullValue());
        assertThat(payload, notNullValue());
        assertThat(metadata.id().toString(), equalTo(ID));
        assertThat(metadata.name(), equalTo(NAME));
        Optional<String> clientCorrelationId = metadata.clientCorrelationId();
        assertThat(clientCorrelationId.get(), equalTo(CLIENT));

        assertThat(metadata.sessionId().get(), equalTo(SESSION));
        assertThat(metadata.userId().get(), equalTo(USER));

        List<UUID> causation = metadata.causation();
        assertThat(causation, notNullValue());
        assertThat(causation.size(), equalTo(2));
        assertThat(causation.get(0).toString(), equalTo(CAUSATION_1));
        assertThat(causation.get(1).toString(), equalTo(CAUSATION_2));

    }

    @Test
    public void shouldReturnJsonObjectFromEnvelope() throws IOException {
        final JsonObjectConverter jsonObjectConverter = new JsonObjectConverter();
        final JsonObject expectedEnvelope = jsonObjectConverter.fromString(jsonFromFile("envelope"));
        final Metadata metadata = JsonObjectMetadata.metadataFrom(expectedEnvelope.getJsonObject(JsonObjectConverter.METADATA));
        final JsonObject payload = jsonObjectConverter.extractPayloadFromEnvelope(expectedEnvelope);

        final Envelope envelope = DefaultEnvelope.envelopeFrom(metadata, payload);

        assertThat(jsonObjectConverter.fromEnvelope(envelope), equalTo(expectedEnvelope));
    }

    private JsonObject envelopeAsJsonObject() {

        JsonObjectBuilder envelopeBuilder = Json.createObjectBuilder();

        JsonObjectBuilder metadataBuilder = Json.createObjectBuilder();
        metadataBuilder.add(JsonObjectMetadata.ID, ID);
        metadataBuilder.add(JsonObjectMetadata.NAME, NAME);

        envelopeBuilder.add(JsonObjectConverter.METADATA, metadataBuilder.build());
        envelopeBuilder.add("payloadId", PAYLOAD_ID);
        envelopeBuilder.add("payloadName", PAYLOAD_NAME);
        envelopeBuilder.add("payloadVersion", PAYLOAD_VERSION);

        return envelopeBuilder.build();
    }

    private String jsonFromFile(final String name) throws IOException {
        return Resources.toString(Resources.getResource(String.format("json/%s.json", name)), Charset.defaultCharset());
    }

}