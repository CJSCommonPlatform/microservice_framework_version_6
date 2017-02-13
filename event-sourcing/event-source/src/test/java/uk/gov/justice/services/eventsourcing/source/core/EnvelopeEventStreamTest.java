package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnvelopeEventStreamTest {

    public static final Long VERSION = 5L;
    private static final UUID STREAM_ID = UUID.randomUUID();
    @Mock
    EventStreamManager eventStreamManager;

    @Mock
    Stream<JsonEnvelope> stream;

    private EventStream eventStream;

    @Before
    public void setup() {
        eventStream = new EnvelopeEventStream(STREAM_ID, eventStreamManager);
    }

    @Test
    public void shouldReturnStreamOfEnvelopes() throws Exception {
        eventStream.read();

        verify(eventStreamManager).read(STREAM_ID);
    }

    @Test
    public void shouldReturnStreamFromVersion() throws Exception {
        eventStream.readFrom(VERSION);

        verify(eventStreamManager).readFrom(STREAM_ID, VERSION);
    }

    @Test
    public void shouldAppendStream() throws Exception {
        eventStream.append(stream);

        verify(eventStreamManager).append(STREAM_ID, stream);
    }

    @Test
    public void shouldAppendStreamAfterVersion() throws Exception {
        eventStream.appendAfter(stream, VERSION);

        verify(eventStreamManager).appendAfter(STREAM_ID, stream, VERSION);
    }

    @Test
    public void shouldReturnCurrentVersion() throws Exception {
        eventStream.getCurrentVersion();

        verify(eventStreamManager).getCurrentVersion(STREAM_ID);
    }

    @Test
    public void testGetId() throws Exception {
        final UUID actualId = eventStream.getId();

        assertThat(actualId, equalTo(STREAM_ID));
    }

}
