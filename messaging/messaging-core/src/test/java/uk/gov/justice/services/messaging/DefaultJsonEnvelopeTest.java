package uk.gov.justice.services.messaging;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.json.JsonObject;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;

/**
 * Unit tests for the {@link DefaultJsonEnvelope} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJsonEnvelopeTest {

    @Mock
    private Metadata metadata;

    @Mock
    private JsonObject payload;

    private JsonEnvelope jsonEnvelope;

    @Before
    public void setup() {
        jsonEnvelope = envelopeFrom(metadata, payload);
    }

    @Test
    public void shouldReturnMetadata() throws Exception {
        assertThat(jsonEnvelope.metadata(), equalTo(metadata));
    }

    @Test
    public void shouldReturnPayload() throws Exception {
        assertThat(jsonEnvelope.payloadAsJsonObject(), equalTo(payload));
    }
}
