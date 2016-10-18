package uk.gov.justice.services.test.utils.core.enveloper;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;

import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;

public class EnvelopeFactoryTest {

    private EnvelopeFactory envelopeFactory = new EnvelopeFactory();

    @Test
    public void shouldCreateAJsonEnvelope() throws Exception {

        final String commandName = "some.command-or-other";

        final JsonObject payload = Json.createObjectBuilder()
                .add("payloadName", "payloadValue")
                .build();

        final JsonEnvelope jsonEnvelope = envelopeFactory.create(commandName, payload);

        assertThat(jsonEnvelope, jsonEnvelope(
                metadata().withName(commandName),
                payloadIsJson(
                        withJsonPath("$.payloadName", equalTo("payloadValue"))
                )));
    }
}
