package uk.gov.justice.services.messaging.spi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;

import uk.gov.justice.services.messaging.Metadata;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@link DefaultJsonEnvelope} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultEnvelopeTest {

    @Mock
    private Metadata metadata;

    @Mock
    private Object payload;

    @Test
    public void shouldReturnMetadata() {
        assertThat(envelopeFrom(metadata, payload).metadata(), equalTo(metadata));
    }

    @Test
    public void shouldReturnPayload() {
        assertThat(envelopeFrom(metadata, payload).payload(), equalTo(payload));
    }
}