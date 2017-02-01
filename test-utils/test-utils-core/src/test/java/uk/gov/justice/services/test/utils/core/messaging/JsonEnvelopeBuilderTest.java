package uk.gov.justice.services.test.utils.core.messaging;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.math.BigDecimal.ONE;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import java.math.BigDecimal;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.junit.Test;
import org.mockito.Mock;

public class JsonEnvelopeBuilderTest {

    @Mock
    private Metadata metadata;

    @Mock
    private JsonValue payloadAsJsonValue;

    @Mock
    private JsonObject payloadAsJsonObject;

    @Mock
    private JsonArray payloadAsJsonArray;

    @Mock
    private JsonNumber payloadAsJsonNumber;

    @Mock
    private JsonString payloadAsJsonString;


    @Test
    public void shouldReturnMetadata() {
        assertThat(envelopeFrom(metadata, payloadAsJsonObject).metadata(), equalTo(metadata));
    }

    @Test
    public void shouldReturnPayloadAsJsonValue() {
        assertThat(envelopeFrom(metadata, payloadAsJsonValue).payload(), equalTo(payloadAsJsonValue));
    }

    @Test
    public void shouldReturnPayloadAsJsonObject() {
        assertThat(envelopeFrom(metadata, payloadAsJsonObject).payloadAsJsonObject(), equalTo(payloadAsJsonObject));
    }

    @Test
    public void shouldReturnPayloadAsJsonArray() {
        assertThat(envelopeFrom(metadata, payloadAsJsonArray).payloadAsJsonArray(), equalTo(payloadAsJsonArray));
    }

    @Test
    public void shouldReturnPayloadAsJsonNumber() {
        assertThat(envelopeFrom(metadata, payloadAsJsonNumber).payloadAsJsonNumber(), equalTo(payloadAsJsonNumber));
    }

    @Test
    public void shouldReturnPayloadAsJsonString() {
        assertThat(envelopeFrom(metadata, payloadAsJsonString).payloadAsJsonString(), equalTo(payloadAsJsonString));
    }

    @Test
    public void shouldPrettyPrintAsJsonWhenCallingToDebugString() throws Exception {

        final String metadataName = "metadata name";
        final UUID metadataId = randomUUID();
        final String payloadName = "payloadName";
        final String payloadValue = "payloadValue";

        final Metadata metadata = metadata(metadataId, metadataName);
        final JsonObject payload = payload(payloadName, payloadValue);
        final JsonEnvelope jsonEnvelope = envelopeFrom(metadata, payload);

        final String json = jsonEnvelope.toDebugStringPrettyPrint();
        with(json)
                .assertEquals("_metadata.id", metadataId.toString())
                .assertEquals("_metadata.name", metadataName)
                .assertEquals("$.payloadName", payloadValue);
    }

    @Test
    public void shouldReturnEnvelopeAsJsonObject() {
        final String metadataName = "metadata name";
        final UUID metadataId = randomUUID();
        final String payloadName = "payloadName";
        final String payloadValue = "payloadValue";

        final Metadata metadata = metadata(metadataId, metadataName);
        final JsonObject payload = payload(payloadName, payloadValue);
        final JsonEnvelope jsonEnvelope = envelopeFrom(metadata, payload);

        final JsonObject jsonObject = jsonEnvelope.asJsonObject();

        with(jsonObject.toString())
                .assertEquals("_metadata.id", metadataId.toString())
                .assertEquals("_metadata.name", metadataName)
                .assertEquals("$.payloadName", payloadValue);
    }

    @Test
    public void shouldReturnComplexEnvelopeAsJsonObject() {
        final String metadataName = "metadata name";
        final UUID metadataId = randomUUID();
        final JsonObjectMetadata.Builder metadata = metadataOf(metadataId, metadataName);
        final UUID testId = randomUUID();

        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadata)
                .withPayloadOf(1, "int")
                .withPayloadOf(Boolean.FALSE, "bool")
                .withPayloadOf("String", "string")
                .withPayloadOf(ONE, "bigd")
                .withPayloadOf(testId, "uuid")
                .withPayloadOf(Json.createObjectBuilder().add("someData", "data").build(), "jsonObject")
                .build();

        final JsonObject jsonObject = jsonEnvelope.asJsonObject();

        with(jsonObject.toString())
                .assertEquals("_metadata.id", metadataId.toString())
                .assertEquals("_metadata.name", metadataName)
                .assertEquals("$.int", 1)
                .assertEquals("$.bool", false)
                .assertEquals("$.string", "String")
                .assertEquals("$.bigd", 1)
                .assertEquals("$.uuid", testId.toString())
                .assertNotNull("$.jsonObject");
    }

    private Metadata metadata(final UUID metadataId, final String metadataName) {
        return metadataOf(metadataId, metadataName).build();
    }

    private JsonObject payload(final String payloadName, final String payloadValue) {
        final JsonObjectBuilderWrapper jsonObjectBuilderWrapper = new JsonObjectBuilderWrapper();
        jsonObjectBuilderWrapper.add(payloadValue, payloadName);

        return jsonObjectBuilderWrapper.build();
    }
}
