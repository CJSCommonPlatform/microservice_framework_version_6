package uk.gov.justice.services.test.utils.core.matchers;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonValueNullMatcher.isJsonValueNull;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.json.JsonValue;

import org.junit.Test;

public class JsonValueEnvelopeMatcherTest {

    private static final UUID ID = randomUUID();
    private static final String NAME = "someName";

    @Test
    public void shouldMatchJsonEnvelope() {

        assertThat(jsonEnvelope(), JsonValueEnvelopeMatcher.jsonValueEnvelope(
                metadata().withName("event.action"),
                payloadIsJson(allOf(
                        withJsonPath("$.someId", equalTo(ID.toString())),
                        withJsonPath("$.name", equalTo(NAME)))
                )));
    }

    @Test
    public void shouldMatchJsonEnvelopeWithSchema() {

        assertThat(jsonEnvelope(), JsonValueEnvelopeMatcher.jsonValueEnvelope().thatMatchesSchema());
    }

    @Test
    public void shouldMatchMetadataIfPayloadIsJsonValueNull() {

        assertThat(jsonEnvelopeWithJsonValueNullPayload(), JsonValueEnvelopeMatcher.jsonValueEnvelope()
                .withMetadataOf(metadata().withName("event.action")));
    }

    @Test
    public void shouldMatchPayloadIfPayloadIsJsonValueNull() {

        assertThat(jsonEnvelopeWithJsonValueNullPayload(), JsonValueEnvelopeMatcher.jsonValueEnvelope()
                .withPayloadOf(payload(isJsonValueNull())));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchJsonEnvelopeWithJsonObject() {

        assertThat(jsonEnvelope(), JsonValueEnvelopeMatcher.jsonValueEnvelope()
                .withPayloadOf(payload(isJsonValueNull())));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchJsonEnvelopeWithSchema() {

        final JsonEnvelope invalidJsonEnvelope = envelope()
                .with(metadataWithRandomUUID("event.action"))
                .withPayloadOf(ID.toString(), "someId")
                .build();

        assertThat(invalidJsonEnvelope, JsonValueEnvelopeMatcher.jsonValueEnvelope().thatMatchesSchema());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailToMatchDifferentMetadata() {

        assertThat(jsonEnvelope(), JsonValueEnvelopeMatcher.jsonValueEnvelope(
                metadata().withName("event.not.match"),
                payloadIsJson(allOf(
                        withJsonPath("$.someId", equalTo(ID.toString())),
                        withJsonPath("$.name", equalTo(NAME)))
                )));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailToMatchDifferentPayload() {

        assertThat(jsonEnvelope(), JsonValueEnvelopeMatcher.jsonValueEnvelope(
                metadata().withName("event.action"),
                payloadIsJson(allOf(
                        withJsonPath("$.someId", equalTo(randomUUID().toString())),
                        withJsonPath("$.name", equalTo(NAME)))
                )));
    }

    private Envelope<JsonValue> jsonEnvelope() {
        return envelope()
                .with(metadataWithRandomUUID("event.action"))
                .withPayloadOf(ID.toString(), "someId")
                .withPayloadOf(NAME, "name")
                .build();
    }

    private JsonEnvelope jsonEnvelopeWithJsonValueNullPayload() {
        return envelopeFrom(metadataWithRandomUUID("event.action").build(), JsonValue.NULL);
    }
}