package uk.gov.justice.services.core.json;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonEnvelope.METADATA;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public class PayloadExtractorTest {

    @InjectMocks
    private PayloadExtractor payloadExtractor;

    @Test
    public void shouldRemoveTheMetadataFromAnEnvelopeJsonStringAndConvertToJsonObject() throws Exception {

        final UUID streamId = randomUUID();
        final String commandName = "time-travel-initiated";

        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataWithRandomUUID(commandName)
                        .withStreamId(streamId))
                .withPayloadOf("Jurassic Era", "destination")
                .build();

        final String envelopeJson = jsonEnvelope.toDebugStringPrettyPrint();

        final JSONObject jsonObject = payloadExtractor.extractPayloadFrom(envelopeJson);

        final String json = jsonObject.toString();

        with(json)
                .assertThat("$.destination", is("Jurassic Era"))
                .assertNotDefined(METADATA)
        ;
    }
}
