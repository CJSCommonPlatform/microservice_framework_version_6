package uk.gov.justice.services.test.utils.messaging.jms;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecordingJmsEnvelopeSenderTest {

    @InjectMocks
    private RecordingJmsEnvelopeSender recordingJmsEnvelopeSender;

    @Test
    public void shouldRecordSentJsonEnvelope() {

        final String destinationName = "queue.command";
        final JsonEnvelope jsonEnvelope_1 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_2 = mock(JsonEnvelope.class);

        recordingJmsEnvelopeSender.send(jsonEnvelope_1, destinationName);
        recordingJmsEnvelopeSender.send(jsonEnvelope_2, destinationName);

        final List<JsonEnvelope> jsonEnvelopes = recordingJmsEnvelopeSender.envelopesSentTo(destinationName);

        assertThat(jsonEnvelopes.size(), is(2));
        assertThat(jsonEnvelopes, hasItems(jsonEnvelope_1, jsonEnvelope_2));
    }

    @Test
    public void shouldRecordSentJsonEnvelopeForDifferentDestinations() {

        final String destinationName_1 = "queue.command.1";
        final String destinationName_2 = "queue.command.2";
        final JsonEnvelope jsonEnvelope_1 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_2 = mock(JsonEnvelope.class);

        recordingJmsEnvelopeSender.send(jsonEnvelope_1, destinationName_1);
        recordingJmsEnvelopeSender.send(jsonEnvelope_2, destinationName_2);

        final List<JsonEnvelope> jsonEnvelopes_1 = recordingJmsEnvelopeSender.envelopesSentTo(destinationName_1);
        final List<JsonEnvelope> jsonEnvelopes_2 = recordingJmsEnvelopeSender.envelopesSentTo(destinationName_2);

        assertThat(jsonEnvelopes_1.size(), is(1));
        assertThat(jsonEnvelopes_1, hasItems(jsonEnvelope_1));

        assertThat(jsonEnvelopes_2.size(), is(1));
        assertThat(jsonEnvelopes_2, hasItems(jsonEnvelope_2));
    }

    @Test
    public void shouldClearSentJsonEnvelopes() {

        final String destinationName = "queue.command";
        final JsonEnvelope jsonEnvelope_1 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_2 = mock(JsonEnvelope.class);

        recordingJmsEnvelopeSender.send(jsonEnvelope_1, destinationName);
        recordingJmsEnvelopeSender.send(jsonEnvelope_2, destinationName);

        assertThat(recordingJmsEnvelopeSender.envelopesSentTo(destinationName).size(), is(2));

        recordingJmsEnvelopeSender.clear();

        assertThat(recordingJmsEnvelopeSender.envelopesSentTo(destinationName).size(), is(0));
    }
}