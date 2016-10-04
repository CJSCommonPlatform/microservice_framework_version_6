package uk.gov.justice.services.test.utils.core.matchers;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payLoad;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import org.junit.Test;

public class JsonEnvelopeMatcherTest {

    private static final UUID ID = randomUUID();
    private static final String NAME = "someName";

    @Test
    public void shouldMatchJsonEnvelope() throws Exception {
        assertThat(jsonEnvelope(), JsonEnvelopeMatcher.jsonEnvelope(
                metadata().withName("event.action"),
                payLoad().isJson(allOf(
                        withJsonPath("$.someId", equalTo(ID.toString())),
                        withJsonPath("$.name", equalTo(NAME)))
                )));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailToMatchDifferentMetadata() throws Exception {
        assertThat(jsonEnvelope(), JsonEnvelopeMatcher.jsonEnvelope(
                metadata().withName("event.not.match"),
                payLoad().isJson(allOf(
                        withJsonPath("$.someId", equalTo(ID.toString())),
                        withJsonPath("$.name", equalTo(NAME)))
                )));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailToMatchDifferentPayload() throws Exception {
        assertThat(jsonEnvelope(), JsonEnvelopeMatcher.jsonEnvelope(
                metadata().withName("event.action"),
                payLoad().isJson(allOf(
                        withJsonPath("$.someId", equalTo(randomUUID().toString())),
                        withJsonPath("$.name", equalTo(NAME)))
                )));
    }

    private JsonEnvelope jsonEnvelope() {
        return envelope()
                .with(metadataWithRandomUUID("event.action"))
                .withPayloadOf(ID.toString(), "someId")
                .withPayloadOf(NAME, "name")
                .build();
    }
}