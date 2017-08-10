package uk.gov.justice.services.test.utils.core.messaging;


import static com.jayway.jsonassert.JsonAssert.with;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.math.BigDecimal.ONE;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;

import java.util.UUID;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.junit.Test;

public class JsonEnvelopeBuilderTest {

    @Test
    public void shouldBuildJsonEnvelopeFromMetadataAndJsonValue() throws Exception {
        final Metadata metadata = mock(Metadata.class);
        final JsonValue payload = mock(JsonValue.class);

        final JsonEnvelope envelope = JsonEnvelopeBuilder.envelopeFrom(metadata, payload);

        assertThat(envelope.metadata(), is(metadata));
        assertThat(envelope.payload(), is(payload));
    }

    @Test
    public void shouldBuildJsonEnvelopeContainingMetadataAndPayloadFromJsonEnvelope() throws Exception {
        final UUID id = randomUUID();
        final String name = "name";

        final MetadataBuilder metadata = metadataBuilder().withId(id).withName(name);
        final JsonObjectBuilder payload = createObjectBuilder().add("test", "value");

        final JsonEnvelope envelope = new JsonEnvelopeBuilder(
                JsonEnvelope.envelopeFrom(metadata, payload))
                .build();

        assertThat(envelope, jsonEnvelope(
                metadata().withId(id).withName(name),
                payloadIsJson(withJsonPath("$.test", equalTo("value")))));
    }

    @Test
    public void shouldBuildJsonEnvelopeContainingPayloadFromJsonEnvelope() throws Exception {
        final UUID id = randomUUID();
        final String name = "name";

        final MetadataBuilder metadata = metadataBuilder().withId(id).withName(name);
        final JsonObjectBuilder payload = createObjectBuilder().add("test", "value");

        final JsonEnvelope envelope = JsonEnvelopeBuilder.envelope()
                .withPayloadFrom(JsonEnvelope.envelopeFrom(metadata, payload)).build();

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

        final JsonEnvelope jsonEnvelope = JsonEnvelopeBuilder.envelope()
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
    public void shouldBuildJsonEnvelopeWithJsonValueNull() throws Exception {
        final JsonEnvelope jsonEnvelope = JsonEnvelopeBuilder.envelope().withNullPayload().build();
        assertThat(jsonEnvelope.payload(), is(JsonValue.NULL));
    }

    @Test
    public void shouldBuildAsJsonString() {
        final String metadataName = "metadata name";
        final UUID metadataId = randomUUID();
        final MetadataBuilder metadata = metadataOf(metadataId, metadataName);
        final UUID testId = randomUUID();

        final String jsonEnvelope = JsonEnvelopeBuilder.envelope()
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
    public void shouldBuildJsonEnvelopeFromDeprecatedMetadataBuilderAndJsonValue() throws Exception {
        final UUID id = randomUUID();
        final String name = "name";

        final JsonObjectMetadata.Builder metadataBuilder = JsonObjectMetadata.metadataOf(id, name);
        final JsonValue payload = mock(JsonValue.class);

        final JsonEnvelope envelope = JsonEnvelopeBuilder.envelopeFrom(metadataBuilder, payload);

        assertThat(envelope, jsonEnvelope().withMetadataOf(metadata().withId(id).withName(name)));
        assertThat(envelope.payload(), is(payload));
    }

    @Test
    public void shouldBuildJsonEnvelopeFromDeprecatedMetadataBuilderAndJsonObjectBuilder() throws Exception {
        final UUID id = randomUUID();
        final String name = "name";

        final JsonObjectMetadata.Builder metadataBuilder = JsonObjectMetadata.metadataOf(id, name);
        final JsonObjectBuilder jsonObjectBuilder = createObjectBuilder().add("test", "value");

        final JsonEnvelope envelope = JsonEnvelopeBuilder.envelopeFrom(metadataBuilder, jsonObjectBuilder);

        assertThat(envelope, jsonEnvelope(
                metadata().withId(id).withName(name),
                payloadIsJson(withJsonPath("$.test", equalTo("value")))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldBuildWithDeprecatedMetadataBuilder() {
        final String metadataName = "metadata name";
        final UUID metadataId = randomUUID();
        final JsonObjectMetadata.Builder metadata = JsonObjectMetadata.metadataOf(metadataId, metadataName);
        final UUID testId = randomUUID();

        final JsonEnvelope jsonEnvelope = JsonEnvelopeBuilder.envelope()
                .with(metadata)
                .withPayloadOf(1, "int")
                .build();

        assertThat(jsonEnvelope, jsonEnvelope(
                metadata().withId(metadataId).withName(metadataName),
                payloadIsJson(
                        withJsonPath("$.int", equalTo(1))
                )));
    }
}
