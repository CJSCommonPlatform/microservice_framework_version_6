package uk.gov.justice.services.test.utils.core.enveloper;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.CoreMatchers.is;
import static uk.gov.justice.services.test.utils.core.matchers.UuidStringMatcher.isAUuid;

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


        final String json = jsonEnvelope.toDebugStringPrettyPrint();

        with(json)
                .assertThat("$._metadata.name", is(commandName))
                .assertThat("$._metadata.id", isAUuid())
                .assertThat("$.payloadName", is("payloadValue"));
    }
}
