package uk.gov.justice.services.messaging;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import com.google.common.io.Resources;
import org.junit.Test;

public class JsonObjectEnvelopeConverterTest {

    private static final String ID = "861c9430-7bc6-4bf0-b549-6534394b8d65";
    private static final String NAME = "test.command.do-something";
    private static final String CLIENT = "d51597dc-2526-4c71-bd08-5031c79f11e1";
    private static final String SESSION = "45b0c3fe-afe6-4652-882f-7882d79eadd9";
    private static final String USER = "72251abb-5872-46e3-9045-950ac5bae399";
    private static final String CAUSATION_1 = "cd68037b-2fcf-4534-b83d-a9f08072f2ca";
    private static final String CAUSATION_2 = "43464b22-04c1-4d99-8359-82dc1934d763";
    private static final String ARRAY_ITEM_1 = "Array Item 1";
    private static final String ARRAY_ITEM_2 = "Array Item 2";
    private static final String FIELD_NUMBER = "number";
    private static final String METADATA = "_metadata";

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

        JsonEnvelope envelope = jsonObjectEnvelopeConverter.asEnvelope(new StringToJsonObjectConverter().convert(jsonFromFile("envelope")));

        assertThat(envelope, notNullValue());
        Metadata metadata = envelope.metadata();
        JsonObject payload = envelope.payloadAsJsonObject();
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
    public void shouldReturnJsonObjectFromEnvelopeWithObjectPayload() throws IOException {
        final JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter = new JsonObjectEnvelopeConverter();
        final JsonObject expectedEnvelope = new StringToJsonObjectConverter().convert(jsonFromFile("envelope"));
        final Metadata metadata = JsonObjectMetadata.metadataFrom(expectedEnvelope.getJsonObject(METADATA));
        final JsonValue payload = jsonObjectEnvelopeConverter.extractPayloadFromEnvelope(expectedEnvelope);

        final JsonEnvelope envelope = DefaultJsonEnvelope.envelopeFrom(metadata, payload);

        assertThat(jsonObjectEnvelopeConverter.fromEnvelope(envelope), equalTo(expectedEnvelope));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnArrayPayloadType() {
        new JsonObjectEnvelopeConverter().fromEnvelope(envelopeWithArrayPayload());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNumberPayloadType() {
        new JsonObjectEnvelopeConverter().fromEnvelope(envelopeWithNumberPayload());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenProvidedEnvelopeWithoutMetadata() throws IOException {
        final JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter = new JsonObjectEnvelopeConverter();
        final JsonObject expectedEnvelope = new StringToJsonObjectConverter().convert(jsonFromFile("envelope"));
        final JsonValue payload = jsonObjectEnvelopeConverter.extractPayloadFromEnvelope(expectedEnvelope);

        final JsonEnvelope envelope = DefaultJsonEnvelope.envelopeFrom(null, payload);

        jsonObjectEnvelopeConverter.fromEnvelope(envelope);
    }

    private JsonEnvelope envelopeWithArrayPayload() {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        jsonArrayBuilder.add(ARRAY_ITEM_1);
        jsonArrayBuilder.add(ARRAY_ITEM_2);

        return DefaultJsonEnvelope.envelopeFrom(JsonObjectMetadata.metadataFrom(metadata()), jsonArrayBuilder.build());
    }

    private JsonEnvelope envelopeWithNumberPayload() {

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add(FIELD_NUMBER, 100);

        return DefaultJsonEnvelope.envelopeFrom(
                JsonObjectMetadata.metadataFrom(metadata()),
                jsonObjectBuilder.build().getJsonNumber(FIELD_NUMBER)
        );
    }

    private JsonObject metadata() {
        JsonObjectBuilder metadataBuilder = Json.createObjectBuilder();
        metadataBuilder.add(JsonObjectMetadata.ID, ID);
        metadataBuilder.add(JsonObjectMetadata.NAME, NAME);

        return metadataBuilder.build();
    }

    private String jsonFromFile(final String name) throws IOException {
        return Resources.toString(Resources.getResource(String.format("json/%s.json", name)), Charset.defaultCharset());
    }

}
