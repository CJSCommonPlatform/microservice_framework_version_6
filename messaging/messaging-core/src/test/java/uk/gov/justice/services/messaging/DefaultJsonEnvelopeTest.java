package uk.gov.justice.services.messaging;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.math.BigDecimal.ONE;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;

import java.util.UUID;

import javax.json.Json;
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
        assertThat(new DefaultJsonEnvelope(metadata, payloadAsJsonObject).metadata(), equalTo(metadata));
    }

    @Test
    public void shouldReturnPayloadAsJsonValue() {
        assertThat(new DefaultJsonEnvelope(metadata, payloadAsJsonValue).payload(), equalTo(payloadAsJsonValue));
    }

    @Test
    public void shouldReturnPayloadAsJsonObject() {
        assertThat(new DefaultJsonEnvelope(metadata, payloadAsJsonObject).payloadAsJsonObject(), equalTo(payloadAsJsonObject));
    }

    @Test
    public void shouldReturnPayloadAsJsonArray() {
        assertThat(new DefaultJsonEnvelope(metadata, payloadAsJsonArray).payloadAsJsonArray(), equalTo(payloadAsJsonArray));
    }

    @Test
    public void shouldReturnPayloadAsJsonNumber() {
        assertThat(new DefaultJsonEnvelope(metadata, payloadAsJsonNumber).payloadAsJsonNumber(), equalTo(payloadAsJsonNumber));
    }

    @Test
    public void shouldReturnPayloadAsJsonString() {
        assertThat(new DefaultJsonEnvelope(metadata, payloadAsJsonString).payloadAsJsonString(), equalTo(payloadAsJsonString));
    }

    @Test
    public void shouldPrettyPrintAsJsonWhenCallingToDebugString() throws Exception {

        final String metadataName = "metadata name";
        final UUID metadataId = randomUUID();
        final String payloadName = "payloadName";
        final String payloadValue = "payloadValue";

        final Metadata metadata = metadata(metadataId, metadataName);
        final JsonObject payload = payload(payloadName, payloadValue);
        final JsonEnvelope jsonEnvelope = new DefaultJsonEnvelope(metadata, payload);

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
        final JsonEnvelope jsonEnvelope = new DefaultJsonEnvelope(metadata, payload);

        final JsonObject jsonObject = jsonEnvelope.asJsonObject();

        with(jsonObject.toString())
                .assertEquals("_metadata.id", metadataId.toString())
                .assertEquals("_metadata.name", metadataName)
                .assertEquals("$.payloadName", payloadValue);
    }

    @Test
    public void shouldReturnEnvelopeAsString() {
        final UUID metadataId = randomUUID();
        final String metadataName = "nameABC123";

        final JsonObject payload = createObjectBuilder()
                .add("strProperty", "valueA")
                .add("nested", createObjectBuilder()
                        .add("strProperty1", "valueB")
                        .add("uuidProperty1", randomUUID().toString())
                        .add("numProperty1", 34)
                        .add("boolProperty1", true))
                .add("arrayProperty", Json.createArrayBuilder()
                        .add("value1").add("value2").add("value3"))
                .build();

        final JsonEnvelope envelope = new DefaultJsonEnvelope(metadataOf(metadataId, metadataName).build(), payload);

        with(envelope.toString())
                .assertEquals("id", metadataId.toString())
                .assertEquals("name", metadataName)
                .assertNotDefined("strProperty")
                .assertNotDefined("nested.strProperty1")
                .assertNotDefined("nested.uuidProperty1")
                .assertNotDefined("nested.numProperty1")
                .assertNotDefined("nested.boolProperty1")
                .assertNotDefined("arrayProperty");
    }

    @Test
    public void shouldReturnStringRepresentationWithObfuscatedValues() throws Exception {
        final UUID metadataId = randomUUID();
        final String metadataName = "nameABC123";

        final JsonObject payload = createObjectBuilder()
                .add("strProperty", "valueA")
                .add("nested", createObjectBuilder()
                        .add("strProperty1", "valueB")
                        .add("uuidProperty1", randomUUID().toString())
                        .add("numProperty1", 34)
                        .add("boolProperty1", true))
                .add("arrayProperty", Json.createArrayBuilder()
                        .add("value1").add("value2").add("value3"))
                .build();

        final JsonEnvelope envelope = new DefaultJsonEnvelope(metadataOf(metadataId, metadataName).build(), payload);

        with(envelope.toObfuscatedDebugString())
                .assertEquals("_metadata.id", metadataId.toString())
                .assertEquals("_metadata.name", metadataName)
                .assertEquals("strProperty", "xxx")
                .assertEquals("nested.strProperty1", "xxx")
                .assertEquals("nested.uuidProperty1", "xxx")
                .assertEquals("nested.numProperty1", 0)
                .assertEquals("nested.boolProperty1", false)
                .assertThat("arrayProperty", hasItems("xxx", "xxx", "xxx"));
    }

    @Test
    public void shouldReturnComplexEnvelopeAsJsonObject() {
        final String metadataName = "metadata name";
        final UUID metadataId = randomUUID();
        final JsonObjectMetadata.Builder metadata = metadataOf(metadataId, metadataName);
        final UUID testId = randomUUID();

        final JsonEnvelope jsonEnvelope = DefaultJsonEnvelope.envelope()
                .with(metadata)
                .withPayloadOf(1, "int")
                .withPayloadOf(Boolean.FALSE, "bool")
                .withPayloadOf("String", "string")
                .withPayloadOf(ONE, "bigd")
                .withPayloadOf(testId, "uuid")
                .withPayloadOf(new String[]{"value1", "value2", "value3"}, "arrayProperty")
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
                .assertThat("arrayProperty", hasItems("value1", "value2", "value3"))
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
