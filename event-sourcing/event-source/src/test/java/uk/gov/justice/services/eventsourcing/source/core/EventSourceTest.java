package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import javax.enterprise.inject.Default;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceTest {

    private static final UUID STREAM_ID = UUID.randomUUID();

    @Mock
    private EventStreamManager eventStreamManager;

    @InjectMocks
    DefaultEventSource eventSource;


    @Test
    public void shouldReturnEventStream() {
        EnvelopeEventStream eventStream = (EnvelopeEventStream) eventSource.getStreamById(STREAM_ID);

        assertThat(eventStream.getId(), equalTo(STREAM_ID));
        assertThat(eventStream.eventStreamManager, equalTo(eventStreamManager));
    }
}
