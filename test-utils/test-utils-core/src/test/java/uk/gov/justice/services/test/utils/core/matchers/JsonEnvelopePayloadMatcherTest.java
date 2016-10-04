package uk.gov.justice.services.test.utils.core.matchers;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;

public class JsonEnvelopePayloadMatcherTest {

    private static final UUID ID = randomUUID();
    private static final String NAME = "someName";

    @Test
    public void shouldMatchAJsonEnvelopePayload() throws Exception {

        assertThat(payload(), JsonEnvelopePayloadMatcher.payLoad().isJson(allOf(
                withJsonPath("$.someId", equalTo(ID.toString())),
                withJsonPath("$.name", equalTo(NAME))))
        );
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchAJsonEnvelopePayload() throws Exception {
        assertThat(payload(), JsonEnvelopePayloadMatcher.payLoad().isJson(allOf(
                withJsonPath("$.someId", equalTo(ID.toString())),
                withJsonPath("$.name", equalTo("will not match"))))
        );
    }

    private JsonObject payload() {
        return envelope()
                .withPayloadOf(ID.toString(), "someId")
                .withPayloadOf(NAME, "name")
                .build().payloadAsJsonObject();
    }
}