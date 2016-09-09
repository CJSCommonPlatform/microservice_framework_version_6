package uk.gov.justice.services.test.utils.core.enveloper;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonObjects.getString;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;

public class EnveloperFactoryTest {

    @Test
    public void shouldCreateUsableEnveloper() throws Exception {
        final JsonObject inputPayload = Json.createObjectBuilder().add("value", "init").build();
        final JsonObject outputPayload = Json.createObjectBuilder().add("name", "output").build();
        final JsonEnvelope envelope = EnvelopeFactory.createEnvelope("init.name", inputPayload);

        final JsonEnvelope result = EnveloperFactory.createEnveloper().withMetadataFrom(envelope, "expected.name").apply(outputPayload);

        final JsonObject resultJson = result.payloadAsJsonObject();
        assertThat(result.metadata().name(), is("expected.name"));
        assertThat(getString(resultJson, "name"), is(Optional.of("output")));
        assertThat(getString(resultJson, "value"), is(Optional.empty()));
    }
}