package uk.gov.justice.services.messaging;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.Before;
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

    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Before
    public void setup() {
        jsonObjectEnvelopeConverter = new JsonObjectEnvelopeConverter();
        jsonObjectEnvelopeConverter.objectMapper = new ObjectMapperProducer().objectMapper();
    }

    @Test
    public void shouldReturnEnvelope() throws Exception {

        final JsonEnvelope envelope = jsonObjectEnvelopeConverter.asEnvelope(jsonObjectFromFile("envelope"));

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
    public void shouldReturnJsonStringFromEnvelope() throws Exception {
        final JsonObject input = jsonObjectFromFile("envelope");
        final Metadata metadata = metadataFrom(input.getJsonObject(METADATA));
        final JsonValue payload = jsonObjectEnvelopeConverter.extractPayloadFromEnvelope(input);

        final JsonEnvelope envelope = envelopeFrom(metadata, payload);

        assertEquals(jsonFromFile("envelope"), jsonObjectEnvelopeConverter.asJsonString(envelope), true);
    }

    @Test
    public void shouldRemoveNullsInJsonString() throws Exception {
        final JsonObject input = jsonObjectFromFile("envelope-with-null");
        final Metadata metadata = metadataFrom(input.getJsonObject(METADATA));
        final JsonValue payload = jsonObjectEnvelopeConverter.extractPayloadFromEnvelope(input);

        final JsonEnvelope envelope = envelopeFrom(metadata, payload);

        assertEquals(jsonFromFile("envelope-with-field-not-present"), jsonObjectEnvelopeConverter.asJsonString(envelope), true);
    }

    @Test
    public void shouldReturnJsonObjectFromEnvelopeWithObjectPayload() throws IOException {
        final JsonObject expectedEnvelope = jsonObjectFromFile("envelope");
        final Metadata metadata = metadataFrom(expectedEnvelope.getJsonObject(METADATA));
        final JsonValue payload = jsonObjectEnvelopeConverter.extractPayloadFromEnvelope(expectedEnvelope);

        final JsonEnvelope envelope = envelopeFrom(metadata, payload);

        assertThat(jsonObjectEnvelopeConverter.fromEnvelope(envelope), equalTo(expectedEnvelope));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnArrayPayloadType() {
        jsonObjectEnvelopeConverter.fromEnvelope(envelopeWithArrayPayload());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNumberPayloadType() {
        jsonObjectEnvelopeConverter.fromEnvelope(envelopeWithNumberPayload());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenProvidedEnvelopeWithoutMetadata() throws IOException {
        final JsonObject expectedEnvelope = jsonObjectFromFile("envelope");
        final JsonValue payload = jsonObjectEnvelopeConverter.extractPayloadFromEnvelope(expectedEnvelope);

        final JsonEnvelope envelope = envelopeFrom(null, payload);

        jsonObjectEnvelopeConverter.fromEnvelope(envelope);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionIfObjectMapperFails() throws Exception {

        final JsonObject envelopeJsonObject = jsonObjectFromFile("envelope");

        jsonObjectEnvelopeConverter.objectMapper = mock(ObjectMapper.class);
        when(jsonObjectEnvelopeConverter.objectMapper.writeValueAsString(envelopeJsonObject)).thenThrow(new JsonGenerationException("failed"));

        final JsonEnvelope envelope = jsonObjectEnvelopeConverter.asEnvelope(envelopeJsonObject);

        jsonObjectEnvelopeConverter.asJsonString(envelope);
    }

    private JsonEnvelope envelopeWithArrayPayload() {
        JsonArrayBuilder jsonArrayBuilder = createArrayBuilder();
        jsonArrayBuilder.add(ARRAY_ITEM_1);
        jsonArrayBuilder.add(ARRAY_ITEM_2);

        return envelopeFrom(metadataFrom(metadata()), jsonArrayBuilder.build());
    }

    private JsonEnvelope envelopeWithNumberPayload() {

        JsonObjectBuilder jsonObjectBuilder = createObjectBuilder();
        jsonObjectBuilder.add(FIELD_NUMBER, 100);

        return envelopeFrom(
                metadataFrom(metadata()),
                jsonObjectBuilder.build().getJsonNumber(FIELD_NUMBER)
        );
    }

    private JsonObject metadata() {
        JsonObjectBuilder metadataBuilder = createObjectBuilder();
        metadataBuilder.add(JsonObjectMetadata.ID, ID);
        metadataBuilder.add(JsonObjectMetadata.NAME, NAME);

        return metadataBuilder.build();
    }

    private String jsonFromFile(final String name) throws IOException {
        return Resources.toString(Resources.getResource(String.format("json/%s.json", name)), Charset.defaultCharset());
    }

    private JsonObject jsonObjectFromFile(final String name) throws IOException {
        try (final JsonReader reader = Json.createReader(new StringReader(jsonFromFile(name)))) {
            return reader.readObject();
        }
    }
}
