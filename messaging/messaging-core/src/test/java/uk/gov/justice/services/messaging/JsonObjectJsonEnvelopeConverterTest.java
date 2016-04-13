package uk.gov.justice.services.messaging;

import com.google.common.io.Resources;
import org.junit.Test;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;

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
import static uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter.METADATA;

public class JsonObjectJsonEnvelopeConverterTest {

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
        final JsonObject joEnvelope = new StringToJsonObjectConverter().convert(jsonFromFile("json/envelope-missing-id.json"));
        JsonObjectMetadata.metadataFrom(joEnvelope.getJsonObject(METADATA));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnMissingName() throws Exception {
        final JsonObject joEnvelope = new StringToJsonObjectConverter().convert(jsonFromFile("envelope-missing-name"));
        JsonObjectMetadata.metadataFrom(joEnvelope.getJsonObject(METADATA));
    }

    @Test
    public void shouldReturnEnvelope() throws Exception {
        final JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter = new JsonObjectEnvelopeConverter();

        JsonEnvelope jsonEnvelope = jsonObjectEnvelopeConverter.asEnvelope(new StringToJsonObjectConverter().convert(jsonFromFile("envelope")));

        assertThat(jsonEnvelope, notNullValue());
        Metadata metadata = jsonEnvelope.metadata();
        JsonObject payload = jsonEnvelope.payloadAsJsonObject();
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
        final JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter = new JsonObjectEnvelopeConverter();
        final JsonObject expectedEnvelope = new StringToJsonObjectConverter().convert(jsonFromFile("envelope"));
        final Metadata metadata = JsonObjectMetadata.metadataFrom(expectedEnvelope.getJsonObject(METADATA));
        final JsonObject payload = jsonObjectEnvelopeConverter.extractPayloadFromEnvelope(expectedEnvelope);

        final JsonEnvelope jsonEnvelope = DefaultJsonEnvelope.envelopeFrom(metadata, payload);

        assertThat(jsonObjectEnvelopeConverter.fromEnvelope(jsonEnvelope), equalTo(expectedEnvelope));
    }

    private JsonObject envelopeAsJsonObject() {

        JsonObjectBuilder envelopeBuilder = Json.createObjectBuilder();

        JsonObjectBuilder metadataBuilder = Json.createObjectBuilder();
        metadataBuilder.add(JsonObjectMetadata.ID, ID);
        metadataBuilder.add(JsonObjectMetadata.NAME, NAME);

        envelopeBuilder.add(METADATA, metadataBuilder.build());
        envelopeBuilder.add("payloadId", PAYLOAD_ID);
        envelopeBuilder.add("payloadName", PAYLOAD_NAME);
        envelopeBuilder.add("payloadVersion", PAYLOAD_VERSION);

        return envelopeBuilder.build();
    }

    private String jsonFromFile(final String name) throws IOException {
        return Resources.toString(Resources.getResource(String.format("json/%s.json", name)), Charset.defaultCharset());
    }

}