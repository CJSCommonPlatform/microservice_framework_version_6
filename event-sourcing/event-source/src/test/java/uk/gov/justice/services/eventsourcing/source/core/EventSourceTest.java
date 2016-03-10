package uk.gov.justice.services.eventsourcing.source.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceTest {

    private static final UUID STREAM_ID = UUID.randomUUID();

    @InjectMocks
    EventSource eventSource;

    @Mock
    private EventStreamManager eventStreamManager;

    @Test
    public void shouldReturnEventStream() {
        EnvelopeEventStream eventStream = (EnvelopeEventStream) eventSource.getStreamById(STREAM_ID);

        assertThat(eventStream.getId(), equalTo(STREAM_ID));
        assertThat(eventStream.eventStreamManager, equalTo(eventStreamManager));
    }
}
