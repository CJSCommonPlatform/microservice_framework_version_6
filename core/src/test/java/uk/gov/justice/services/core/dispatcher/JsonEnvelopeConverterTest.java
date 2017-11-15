package uk.gov.justice.services.core.dispatcher;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.JsonValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JsonEnvelopeConverterTest {

    @Test
    public void shouldConvertJsonValueEnvelopeToJsonEnvelope() throws JsonProcessingException {
        final Envelope<JsonValue> envelope = mock(Envelope.class);
        final JsonEnvelopeConverter jsonEnvelopeConverter = new JsonEnvelopeConverter();
        final JsonEnvelope jsonEnvelope = jsonEnvelopeConverter.toJsonEnvelope(envelope);
        assertThat(jsonEnvelope, isA(JsonEnvelope.class));
    }

    @Test
    public void shouldHandleJsonEnvelope() throws JsonProcessingException {
        final Envelope<JsonValue> envelope = mock(JsonEnvelope.class);
        final JsonEnvelopeConverter jsonEnvelopeConverter = new JsonEnvelopeConverter();
        final JsonEnvelope jsonEnvelope = jsonEnvelopeConverter.toJsonEnvelope(envelope);
        assertTrue(envelope == jsonEnvelope);
    }
}