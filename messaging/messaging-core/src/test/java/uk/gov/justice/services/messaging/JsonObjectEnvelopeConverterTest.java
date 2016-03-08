package uk.gov.justice.services.messaging;

import com.google.common.io.Resources;
import org.junit.Test;
import uk.gov.justice.services.common.converter.JsonObjectConverter;

import javax.json.Json;
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

public class JsonObjectEnvelopeConverterTest {

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
        final JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter = new JsonObjectEnvelopeConverter();

        Envelope envelope = jsonObjectEnvelopeConverter.asEnvelope(jsonObjectConverter.fromString(jsonFromFile("envelope")));

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
        final JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter = new JsonObjectEnvelopeConverter();
        final JsonObject expectedEnvelope = jsonObjectConverter.fromString(jsonFromFile("envelope"));
        final Metadata metadata = JsonObjectMetadata.metadataFrom(expectedEnvelope.getJsonObject(JsonObjectConverter.METADATA));
        final JsonObject payload = jsonObjectEnvelopeConverter.extractPayloadFromEnvelope(expectedEnvelope);

        final Envelope envelope = DefaultEnvelope.envelopeFrom(metadata, payload);

        assertThat(jsonObjectEnvelopeConverter.fromEnvelope(envelope), equalTo(expectedEnvelope));
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