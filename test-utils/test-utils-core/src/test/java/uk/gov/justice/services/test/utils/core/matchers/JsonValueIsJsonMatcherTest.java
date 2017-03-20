package uk.gov.justice.services.test.utils.core.matchers;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.messaging.DefaultJsonEnvelope;

import java.util.UUID;

import javax.json.JsonValue;

import org.junit.Test;

public class JsonValueIsJsonMatcherTest {

    private static final UUID ID = randomUUID();
    private static final String NAME = "someName";

    @Test
    public void shouldMatchJsonValueAsJson() throws Exception {
        assertThat(payload(), JsonValueIsJsonMatcher.isJson(allOf(
                withJsonPath("$.someId", equalTo(ID.toString())),
                withJsonPath("$.name", equalTo(NAME))))
        );
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchJsonValueAsJsonIfJsonDoesNotMatch() throws Exception {
        assertThat(payload(), JsonValueIsJsonMatcher.isJson(allOf(
                withJsonPath("$.someId", equalTo(ID.toString())),
                withJsonPath("$.name", equalTo("will not match"))))
        );
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchJsonValueAsJsonIfJsonValueIsNotJsonObject() throws Exception {
        assertThat(jsonEnvelopeWithJsonValueNullPayload(), JsonValueIsJsonMatcher.isJson(allOf(
                withJsonPath("$.someId", equalTo(ID.toString())),
                withJsonPath("$.name", equalTo(NAME))))
        );
    }

    private JsonValue payload() {
        return envelope()
                .withPayloadOf(ID.toString(), "someId")
                .withPayloadOf(NAME, "name")
                .build().payload();
    }

    private JsonValue jsonEnvelopeWithJsonValueNullPayload() {
        return new DefaultJsonEnvelope(metadataWithRandomUUID("event.action").build(), JsonValue.NULL).payload();
    }
}