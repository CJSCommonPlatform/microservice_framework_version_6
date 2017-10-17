package uk.gov.justice.services.messaging;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;

import java.util.UUID;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@link DefaultJsonEnvelope} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJsonEnvelopeTest {

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
    public void shouldReturnStringRepresentationWithObfuscatedValues() throws Exception {
        final UUID metadataId = randomUUID();
        final String metadataName = "nameABC123";
        final JsonEnvelope envelope = envelope()
                .with(metadataOf(metadataId, metadataName))
                .withPayloadOf("valueA", "strProperty")
                .withPayloadOf("valueB", "nested", "strProperty1")
                .withPayloadOf(randomUUID(), "nested", "uuidProperty1")
                .withPayloadOf(34, "nested", "numProperty1")
                .withPayloadOf(true, "nested", "boolProperty1")
                .withPayloadOf(new String[]{"value1", "value2", "value3"}, "arrayProperty")
                .build();


        final String obfuscatedDebugString = envelope.toObfuscatedDebugString();

        with(obfuscatedDebugString)
                .assertEquals("_metadata.id", metadataId.toString())
                .assertEquals("_metadata.name", metadataName)
                .assertEquals("strProperty", "xxx")
                .assertEquals("nested.strProperty1", "xxx")
                .assertEquals("nested.uuidProperty1", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
                .assertEquals("nested.numProperty1", 0)
                .assertEquals("nested.boolProperty1", false)
                .assertThat("arrayProperty", hasItems("xxx", "xxx", "xxx"));
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
