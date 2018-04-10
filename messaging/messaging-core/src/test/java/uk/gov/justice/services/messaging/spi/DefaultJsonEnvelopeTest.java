package uk.gov.justice.services.messaging.spi;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;

import java.util.UUID;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
        final String metadataSource = "metadata source";
        final UUID metadataId = randomUUID();
        final String payloadName = "payloadName";
        final String payloadValue = "payloadValue";

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadata(metadataId, metadataName, metadataSource), payload(payloadName, payloadValue));

        final String json = jsonEnvelope.toDebugStringPrettyPrint();
        with(json)
                .assertEquals("_metadata.id", metadataId.toString())
                .assertEquals("_metadata.name", metadataName)
                .assertEquals("_metadata.source", metadataSource)
                .assertEquals("$.payloadName", payloadValue);
    }

    @Test
    public void shouldReturnEnvelopeAsJsonObject() {
        final String metadataName = "metadata name";
        final String metadataSource = "metadata source";
        final UUID metadataId = randomUUID();
        final String payloadName = "payloadName";
        final String payloadValue = "payloadValue";

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadata(metadataId, metadataName, metadataSource), payload(payloadName, payloadValue));

        final JsonObject jsonObject = jsonEnvelope.asJsonObject();

        with(jsonObject.toString())
                .assertEquals("_metadata.id", metadataId.toString())
                .assertEquals("_metadata.name", metadataName)
                .assertEquals("_metadata.source", metadataSource)
                .assertEquals("$.payloadName", payloadValue);
    }

    @Test
    public void shouldReturnEnvelopeAsString() {
        final UUID metadataId = randomUUID();
        final String metadataName = "nameABC123";
        final String source = "sourceName";

        final JsonEnvelope envelope = envelopeFrom(
                metadataBuilder().withId(metadataId).withName(metadataName).withSource(source),
                createObjectBuilder()
                        .add("strProperty", "valueA")
                        .add("nested", createObjectBuilder()
                                .add("strProperty1", "valueB")
                                .add("uuidProperty1", randomUUID().toString())
                                .add("numProperty1", 34)
                                .add("boolProperty1", true))
                        .add("arrayProperty", createArrayBuilder()
                                .add("value1")
                                .add("value2")
                                .add("value3"))
                        .build());

        with(envelope.toString())
                .assertEquals("id", metadataId.toString())
                .assertEquals("name", metadataName)
                .assertEquals("source" , source)
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
        final String source = "sourceName";

        final JsonEnvelope envelope = envelopeFrom(
                metadataBuilder().withId(metadataId).withName(metadataName).withSource(source),
                createObjectBuilder()
                        .add("strProperty", "valueA")
                        .add("nested", createObjectBuilder()
                                .add("strProperty1", "valueB")
                                .add("uuidProperty1", randomUUID().toString())
                                .add("numProperty1", 34)
                                .add("boolProperty1", true))
                        .add("arrayProperty", createArrayBuilder()
                                .add("value1")
                                .add("value2")
                                .add("value3"))
                        .build());

        with(envelope.toObfuscatedDebugString())
                .assertEquals("_metadata.id", metadataId.toString())
                .assertEquals("_metadata.name", metadataName)
                .assertEquals("_metadata.source", source)
                .assertEquals("strProperty", "xxx")
                .assertEquals("nested.strProperty1", "xxx")
                .assertEquals("nested.uuidProperty1", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
                .assertEquals("nested.numProperty1", 0)
                .assertEquals("nested.boolProperty1", false)
                .assertThat("arrayProperty", hasItems("xxx", "xxx", "xxx"));
    }

    private MetadataBuilder metadata(final UUID metadataId, final String metadataName, final String metadataSource) {
        return metadataBuilder().withId(metadataId).withName(metadataName).withSource(metadataSource);
    }

    private JsonObjectBuilder payload(final String payloadName, final String payloadValue) {
        return createObjectBuilder().add(payloadName, payloadValue);
    }
}
