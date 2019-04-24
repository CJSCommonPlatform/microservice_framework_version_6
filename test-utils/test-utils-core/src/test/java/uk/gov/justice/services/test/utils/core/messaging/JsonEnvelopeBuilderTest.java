package uk.gov.justice.services.test.utils.core.messaging;


import static com.jayway.jsonassert.JsonAssert.with;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.math.BigDecimal.ONE;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;

import java.util.UUID;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.junit.Test;

public class JsonEnvelopeBuilderTest {

    @Test
    public void shouldBuildJsonEnvelopeContainingMetadataAndPayloadFromJsonEnvelope() {
        final UUID id = randomUUID();
        final String name = "name";

        final MetadataBuilder metadata = metadataBuilder().withId(id).withName(name);
        final JsonObjectBuilder payload = createObjectBuilder().add("test", "value");

        final JsonEnvelope envelope = new JsonEnvelopeBuilder(
                envelopeFrom(metadata, payload))
                .build();

        assertThat(envelope, jsonEnvelope(
                metadata().withId(id).withName(name),
                payloadIsJson(withJsonPath("$.test", equalTo("value")))));
    }

    @Test
    public void shouldBuildJsonEnvelopeContainingPayloadFromJsonEnvelope() {
        final UUID id = randomUUID();
        final String name = "name";

        final MetadataBuilder metadata = metadataBuilder().withId(id).withName(name);
        final JsonObjectBuilder payload = createObjectBuilder().add("test", "value");

        final JsonEnvelope envelope = envelope()
                .withPayloadFrom(envelopeFrom(metadata, payload)).build();

        assertThat(envelope.metadata(), nullValue());
        assertThat(envelope, jsonEnvelope()
                .withPayloadOf(payloadIsJson(withJsonPath("$.test", equalTo("value")))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldBuildComplexEnvelopePayload() {
        final String metadataName = "metadata name";
        final UUID metadataId = randomUUID();
        final MetadataBuilder metadata = metadataOf(metadataId, metadataName);
        final UUID testId = randomUUID();

        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadata)
                .withPayloadOf(1, "int")
                .withPayloadOf(Boolean.FALSE, "bool")
                .withPayloadOf("String", "string")
                .withPayloadOf(ONE, "bigd")
                .withPayloadOf(testId, "uuid")
                .withPayloadOf(new String[]{"value1", "value2", "value3"}, "arrayProperty")
                .withPayloadOf(createObjectBuilder().add("someData", "data").build(), "jsonObject")
                .build();

        assertThat(jsonEnvelope, jsonEnvelope(
                metadata().withId(metadataId).withName(metadataName),
                payloadIsJson(allOf(
                        withJsonPath("$.int", equalTo(1)),
                        withJsonPath("$.bool", equalTo(false)),
                        withJsonPath("$.string", equalTo("String")),
                        withJsonPath("$.bigd", equalTo(1)),
                        withJsonPath("$.uuid", equalTo(testId.toString())),
                        withJsonPath("$.arrayProperty", hasItems("value1", "value2", "value3")),
                        withJsonPath("$.jsonObject.someData", equalTo("data"))
                ))));
    }

    @Test
    public void shouldBuildJsonEnvelopeWithJsonValueNull() {
        final JsonEnvelope jsonEnvelope = envelope().withNullPayload().build();
        assertThat(jsonEnvelope.payload(), is(JsonValue.NULL));
    }

    @Test
    public void shouldBuildAsJsonString() {
        final String metadataName = "metadata name";
        final UUID metadataId = randomUUID();
        final MetadataBuilder metadata = metadataOf(metadataId, metadataName);
        final UUID testId = randomUUID();

        final String jsonEnvelope = envelope()
                .with(metadata)
                .withPayloadOf(1, "int")
                .withPayloadOf(Boolean.FALSE, "bool")
                .withPayloadOf("String", "string")
                .withPayloadOf(ONE, "bigd")
                .withPayloadOf(testId, "uuid")
                .withPayloadOf(new String[]{"value1", "value2", "value3"}, "arrayProperty")
                .withPayloadOf(createObjectBuilder().add("someData", "data").build(), "jsonObject")
                .toJsonString();

        with(jsonEnvelope)
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

    @Test
    public void shouldBuildWithDeprecatedMetadataBuilder() {
        final String metadataName = "metadata name";
        final UUID metadataId = randomUUID();

        final JsonEnvelope jsonEnvelope = envelopeFrom(
                metadataBuilder().withId(metadataId).withName(metadataName),
                createObjectBuilder().add("int", 1));

        assertThat(jsonEnvelope, jsonEnvelope(
                metadata().withId(metadataId).withName(metadataName),
                payloadIsJson(
                        withJsonPath("$.int", equalTo(1))
                )));
    }

    @Test
    public void shouldBuildJsonEnvelopeWithFullyConstructedRootObjectPayload() {
        final String attributeName = "key";
        final String attributeValue = "attributeValue";
        final JsonObject rootObject = createObjectBuilder().add(attributeName, attributeValue).build();
        final JsonEnvelope jsonEnvelope = envelope().withPayloadFrom(rootObject).build();
        assertThat(jsonEnvelope.payload(), notNullValue());
        assertThat(jsonEnvelope.payloadAsJsonObject().getString(attributeName), is(attributeValue));
    }

    @Test
    public void shouldDiscardExistingStateAndRebuildUsingFullyConstructedRootObjectPayload() {
        final JsonEnvelopeBuilder jsonEnvelopeBuilder = envelope();

        final String attributeName = "key";
        final String initialAttributeValue = "finalAttributeValue";
        final String finalAttributeValue = "finalAttributeValue";
        // add a top level attribute
        jsonEnvelopeBuilder.withPayloadOf(initialAttributeValue, attributeName);

        final JsonObject rootObject = createObjectBuilder().add(attributeName, finalAttributeValue).build();
        // overwrite with full payload
        final JsonEnvelope jsonEnvelope = jsonEnvelopeBuilder.withPayloadFrom(rootObject).build();

        final JsonObject actualPayload = jsonEnvelope.payloadAsJsonObject();
        assertThat(actualPayload, notNullValue());
        assertThat(actualPayload.getString(attributeName), is(finalAttributeValue));
    }

    @Test
    public void shouldBuildJsonEnvelopeWithJsonArrayAttribute() {
        final JsonArray numberJsonArray = createArrayBuilder().add(1).add(2).build();
        final String arrayAttributeName = "numbers";
        final JsonEnvelope jsonEnvelope = envelope().withPayloadOf(numberJsonArray, arrayAttributeName).build();
        assertThat(jsonEnvelope.payload(), notNullValue());
        assertThat(jsonEnvelope.payloadAsJsonObject().getJsonArray(arrayAttributeName), hasSize(2));
    }
}
